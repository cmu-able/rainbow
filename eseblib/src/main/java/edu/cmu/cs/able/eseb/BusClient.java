package edu.cmu.cs.able.eseb;

import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.exh.LocalCollector;
import incubator.pval.Ensure;
import incubator.wt.CloseableListener;
import incubator.wt.WorkerThread;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Client that can be used to send and receive data from the bus. The client
 * will establish the connection and re-establish it if it drops. Data can
 * be sent without the connection being established: it will be buffered and
 * sent when possible.
 */
public class BusClient implements Closeable {
	/**
	 * Time to "cool down" after an unsuccessful connection attempt. This is
	 * used to prevent cycling very fast on connection attempts.
	 */
	private static final long ESTABLISHER_COOLING_PERIOD_MS = 5_000;
	
	/**
	 * Logger to use.
	 */
	private static final Logger LOG = Logger.getLogger(BusClient.class);
	
	/**
	 * The host to connect to.
	 */
	private String m_host;
	
	/**
	 * The port to connect to.
	 */
	private short m_port;
	
	/**
	 * The client state.
	 */
	private BusClientState m_state;
	
	/**
	 * The local dispatcher.
	 */
	private LocalDispatcher<BusClientListener> m_dispatcher;
	
	/**
	 * Buffer with pending data values to send.
	 */
	private LinkedList<DataValue> m_out_buffer;
	
	/**
	 * The socket connection if the client is connected. <code>null</code> if
	 * the client is not connected.
	 */
	private ControlledDataTypeSocketConnection m_connection;
	
	/**
	 * Worker thread that establishes the connection. <code>null</code> if
	 * the client is not connecting.
	 */
	private WorkerThread m_connection_establisher;
	
	/**
	 * Time at which the connection was established if the client is connected
	 * and the time at which the client was disconnected if the client is
	 * disconnected or connecting.
	 */
	private Date m_connection_time;
	
	/**
	 * Number of times the client has successfully connected to the server.
	 */
	private int m_connect_count;
	
	/**
	 * Number of values received from the server.
	 */
	private int m_receive_count;
	
	/**
	 * Number of values sent to the server.
	 */
	private int m_send_count;
	
	/**
	 * Exception collector.
	 */
	private LocalCollector m_collector;
	
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_primitive_scope;
	
	/**
	 * Queue used to receive data from the underlaying connection.
	 */
	private BusDataQueue m_queue;
	
	/**
	 * Queue group to inform client listeners.
	 */
	private BusDataQueueGroupImpl m_queue_group;
	
	/**
	 * Creates a new client.
	 * @param host the host to connect to
	 * @param port the port
	 * @param scope the data type scope
	 */
	public BusClient(String host, short port, PrimitiveScope scope) {
		Ensure.not_null(host);
		Ensure.greater(port, 0);
		Ensure.not_null(scope);
		
		m_host = host;
		m_port = port;
		m_state = BusClientState.DISCONNECTED;
		m_dispatcher = new LocalDispatcher<>();
		m_out_buffer = new LinkedList<>();
		m_connection = null;
		m_connection_establisher = null;
		m_connection_time = new Date();
		m_connect_count = 0;
		m_receive_count = 0;
		m_send_count = 0;
		m_collector = new LocalCollector("BusClient (" + host + ":" + port
				+ ")");
		m_primitive_scope = scope;
		m_queue = new BusDataQueue();
		m_queue.dispatcher().add(new BusDataQueueListener() {
			@Override
			public void data_added_to_queue() {
				received();
			}
		});
		
		m_queue_group = new BusDataQueueGroupImpl(); 
	}
	
	/**
	 * Starts the bus client. This will establish the connection if
	 * necessary. This method doesn't do anything if invoked when the
	 * client is already running.
	 */
	public synchronized void start() {
		switch (m_state) {
		case CONNECTED:
		case CONNECTING:
			break;
		case DISCONNECTED:
			switch_disconnected_connecting();
			break;
		default:
			assert false;
		}
	}
	
	/**
	 * Stops and disconnects the client.
	 */
	public synchronized void stop() {
		switch (m_state) {
		case CONNECTED:
			switch_connected_disconnected();
			break;
		case CONNECTING:
			switch_connecting_disconnected();
			break;
		case DISCONNECTED:
			break;
		default:
			assert false;
		}
	}
	
	/**
	 * Adds a listener that is informed of changes in the client and of
	 * received values
	 * @param l the listener
	 */
	public void add_listener(BusClientListener l) {
		m_dispatcher.add(l);
	}
	
	/**
	 * Removes a previously added listener.
	 * @param l the listener
	 */
	public void remove_listener(BusClientListener l) {
		m_dispatcher.remove(l);
	}
	
	/**
	 * Obtains the current state of the client.
	 * @return the state
	 */
	public synchronized BusClientState state() {
		return m_state;
	}
	
	/**
	 * Obtains the time at which the client connected or disconnected
	 * depending whether the current state is connected or
	 * connecting/disconnected.
	 * @return the time
	 */
	public synchronized Date connection_time() {
		return m_connection_time;
	}
	
	/**
	 * Obtains the number of values received by the client.
	 * @return the number of values
	 */
	public synchronized int receive_count() {
		return m_receive_count;
	}
	
	/**
	 * Obtains the number of values sent by the client.
	 * @return the number of values
	 */
	public synchronized int sent_count() {
		return m_send_count;
	}
	
	/**
	 * Obtains the number of times the client has successfully connected
	 * to the server.
	 * @return the count
	 */
	public synchronized int connect_count() {
		return m_connect_count;
	}
	
	/**
	 * Sends a data type to the server or queues it locally if the connection
	 * to the server has not yet been established. Note that value delivery
	 * is not guaranteed if the connection is lost.
	 * @param value the value to send
	 */
	public synchronized void send(DataValue value) {
		Ensure.notNull(value);
		m_send_count++;
		
		switch (m_state) {
		case CONNECTED:
			/*
			 * We want to preserve ordering of values sent, so if the buffer
			 * has values, we need to send those first (and there should be
			 * a dispatched event to do that).
			 */
			if (m_out_buffer.size() > 0) {
				LOG.debug("Queueing: " + value);
				m_out_buffer.addLast(value);
			} else {
				try {
					LOG.debug("Writing (online): " + value);
					m_connection.write(value);
				} catch (IOException e) {
					m_collector.collect(e, "send");
					LOG.error("Error while writing {" + value + "}.", e);
				}
			}
			
			break;
		case CONNECTING:
		case DISCONNECTED:
			m_out_buffer.add(value);
			break;
		default:
			assert false;
		}
	}
	
	/**
	 * Switches from DISCONNECTED to CONNECTING state.
	 */
	private synchronized void switch_disconnected_connecting() {
		assert m_state == BusClientState.DISCONNECTED;
		
		m_state = BusClientState.CONNECTING;
		m_connection_establisher = new WorkerThread("Client connect ("
				+ m_host + ":" + m_port + ")") {
			@Override
			protected void do_cycle_operation() throws Exception {
				establisher_cycle();
			}
		};
		
		m_connection_establisher.start();
		notify_state_changed();
	}
	
	/**
	 * Switches from CONNECTED to DISCONNECTED state.
	 */
	private synchronized void switch_connected_disconnected() {
		assert m_state == BusClientState.CONNECTED;
		
		try {
			m_connection.close();
		} catch (IOException e) {
			m_collector.collect(e, "connect->disconnect");
			LOG.error("Error while closing connection.", e);
		}
		
		m_connection.stop();
		
		m_state = BusClientState.DISCONNECTED; 
		m_connection = null;
		m_connection_time = new Date();
		notify_state_changed();
	}
	
	/**
	 * Switches from CONNECTING to DISCONNECTED state.
	 */
	private synchronized void switch_connecting_disconnected() {
		assert m_state == BusClientState.CONNECTING;
		
		/*
		 * Although not strictly necessary, if we set the state to DISCONNECTED
		 * before asking for the establisher thread to stop (the method will
		 * only return after the stop) we may avoid the cooling period of
		 * the establisher if we hit while waiting for the connection. It is
		 * still possible, due to a race condition, that we may have to wait
		 * for the cooling period anyway.
		 */
		m_state = BusClientState.DISCONNECTED;
		m_connection_establisher.stop();
		m_connection_establisher = null;
		notify_state_changed();
	}
	
	/**
	 * Switches from CONNECTING to CONNECTED state.
	 * @param s the connection socket
	 * @throws IOException failed to modify the state
	 */
	private synchronized void switch_connecting_connected(Socket s)
			throws IOException {
		assert m_state == BusClientState.CONNECTING;
		assert s != null;
		
		WorkerThread establisher = m_connection_establisher;
		
		m_state = BusClientState.CONNECTED;
		
		/*
		 * The Eclipse warning on potential resource leak is true. If the
		 * controlled connection constructor fails with an exception we'll
		 * leak the socket connection. However, this is untestable because
		 * the controlled connection cannot throw an exception in any
		 * testable situation.  
		 */
		@SuppressWarnings("resource")
		ControlledDataTypeSocketConnectionImpl impl = 
				new ControlledDataTypeSocketConnectionImpl(
				m_primitive_scope,
				new DataTypeSocketConnectionImpl("BusClient " + m_host
						+ ":" + m_port, s, new DefaultTextEncoding(),
						m_primitive_scope));
		m_connection = impl;
		m_connection_establisher = null;
		m_connection_time = new Date();
		m_connect_count++;
		
		send_clear_out_buffer();
		
		m_connection.queue_group().add(m_queue);
		
		m_connection.closeable_dispatcher().add(new CloseableListener() {
			@Override
			public void closed(IOException e) {
				/*
				 * If we're not in connected state, then a close is irrelevant.
				 * It may have been generated on purpose when the connection
				 * is closed.
				 */
				synchronized (BusClient.this) {
					if (m_state == BusClientState.CONNECTED) {
						switch_connected_connecting();
					}
				}
			}
		});
		
		m_connection.start();
		notify_state_changed();
		
		/*
		 * This may fire an exception if it is the establisher that is
		 * trying to stop itself. That is fine because the exception will
		 * propagate and will terminate the thread.
		 */
		establisher.stop();
	}
	
	/**
	 * Switches from CONNECTED to CONNECTING
	 */
	private void switch_connected_connecting() {
		switch_connected_disconnected();
		switch_disconnected_connecting();
	}
	
	/**
	 * Invoked from within the establisher thread cycle without any locks on
	 * the current object.
	 * @throws InterruptedException wait interrupted
	 */
	@SuppressWarnings("resource")
	private void establisher_cycle() throws InterruptedException {
		synchronized (this) {
			/*
			 * We only want to establish a connecting if we're connecting.
			 * However, because there is a lot of asynchronous stuff going on
			 * this is not guaranteed to happen.
			 */
			if (m_state != BusClientState.CONNECTING) {
				wait(ESTABLISHER_COOLING_PERIOD_MS);
				return;
			}
		}
		
		/*
		 * Try to connect.
		 */
		Socket s = null;
		try {
			s = new Socket(m_host, m_port);
		} catch (IOException e) {
			m_collector.collect(e, "connect");
			LOG.info("Error while connecting to host " + m_host + ", port "
				+ m_port + ".", e);
		}
		
		/*
		 * If we didn't manage to connect, we should cool a little bit to
		 * avoid spamming connection attempts.
		 */
		synchronized (this) {
			if (s != null) {
				if (m_state == BusClientState.CONNECTING) {
					try {
						switch_connecting_connected(s);
					} catch (IOException e) {
						m_collector.collect(e, "establish after connect");
						LOG.error("Error establishing connection after socket "
								+ "connect.", e);
					}
				} else {
					/*
					 * We managed to connect but now we should disconnect.
					 */
					try {
						s.close();
					} catch (IOException e) {
						m_collector.collect(e, "close unnecessary socket");
						LOG.error("Error while closing unnecessary socket.", e);
					}
					
					s = null;
				}
			}
			
			if (s == null && m_state == BusClientState.CONNECTING) {
				wait(ESTABLISHER_COOLING_PERIOD_MS);
			}
		}
	}
	
	/**
	 * Dispatch an event to send the topmost value in the output buffer if
	 * there is one and trigger another event.
	 */
	private synchronized void send_clear_out_buffer() {
		m_dispatcher.dispatch(new Runnable() {
			@Override
			public void run() {
				synchronized (BusClient.this) {
					if (m_out_buffer.size() > 0
							&& m_state == BusClientState.CONNECTED) {
						DataValue v = m_out_buffer.removeFirst();
						
						try {
							LOG.debug("Writing (deferred): " + v);
							m_connection.write(v);
						} catch (IOException e) {
							m_collector.collect(e, "send from buffer");
							LOG.error("Error while writing {" + v + "}.", e);
						}
						
						send_clear_out_buffer();
					}
				}
			}
		});
	}
	
	/**
	 * Data may have been received in the queue.
	 */
	private synchronized void received() {
		List<BusData> received = m_queue_group.transfer_from(m_queue);
		for (BusData d : received) {
			m_receive_count++;
			LOG.debug("Received: {" + d.value() + "}.");
		}
	}
	
	/**
	 * Notifies all listeners that the client's state has changed.
	 */
	private synchronized void notify_state_changed() {
		m_dispatcher.dispatch(new DispatcherOp<BusClientListener>() {
			@Override
			public void dispatch(BusClientListener l) {
				l.client_state_changed();
			}
		});
	}

	/**
	 * Same as {@link #stop}.
	 */
	@Override
	public void close() throws IOException {
		stop();
	}
	
	/**
	 * Obtains the exception collector.
	 * @return the collector
	 */
	public synchronized LocalCollector collector() {
		return m_collector;
	}
	
	/**
	 * Obtains the queue group to register receiving queues.
	 * @return the group
	 */
	public BusDataQueueGroup queue_group() {
		return m_queue_group;
	}
}
