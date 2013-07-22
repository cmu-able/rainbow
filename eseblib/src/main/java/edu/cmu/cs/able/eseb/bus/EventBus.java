package edu.cmu.cs.able.eseb.bus;

import incubator.ExceptionSuppress;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.exh.LocalCollector;
import incubator.pval.Ensure;
import incubator.wt.CloseableListener;
import incubator.wt.CloseableWorkerThread;
import incubator.wt.WorkerThreadGroup;
import incubator.wt.WorkerThreadGroupCI;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueListener;
import edu.cmu.cs.able.eseb.ControlledDataTypeSocketConnectionImpl;
import edu.cmu.cs.able.eseb.DataTypeSocketConnection;
import edu.cmu.cs.able.eseb.DataTypeSocketConnectionImpl;
import edu.cmu.cs.able.eseb.filter.EventFilterChain;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;

/**
 * <p>Implementation of an event bus which opens a port and receives
 * connections in the port. All messages sent by one connection are generally
 * forward to all other connections.</p>
 * <p>Multiple event buses can coexist in the same process as long as they
 * do not share ports. Information on main events associated with the event bus
 * can be obtained by installing listeners.</p>
 * <p>Accept preprocessors ({@link EventBusAcceptPreprocessor} can be added
 * to the event and will be invoked before a connection is added to the
 * bus.</p>
 * <p>All connections in the bus have a bus-side input filter chain and
 * bus-side output filter chain that are independent of any other chain that
 * may exist in the other side of the connection.</p>
 */
public class EventBus implements Closeable {
	/**
	 * Logger to use.
	 */
	private static final Logger LOG = Logger.getLogger(EventBus.class);
	
	/**
	 * How much time to wait for the socket to time out.
	 */
	private static final int SOCKET_TMEOUT_MS = 100;
	
	/**
	 * Group with all event bus worker threads.
	 */
	private WorkerThreadGroup m_group;
	
	/**
	 * Maps connection IDs with their connections.
	 */
	private Map<Integer, EventBusConnectionData> m_connections;
	
	/**
	 * The socket where connections are received. It will be
	 * <code>null</code> when shut down.
	 */
	private ServerSocket m_accept_socket;
	
	/**
	 * Dispatcher.
	 */
	private LocalDispatcher<EventBusListener> m_dispatcher;
	
	/**
	 * The next ID for a connection.
	 */
	private int m_next_connection_id;
	
	/**
	 * The primitive scope for data types.
	 */
	private PrimitiveScope m_scope;
	
	/**
	 * Exception collector.
	 */
	private LocalCollector m_collector;
	
	/**
	 * The port for incoming connections.
	 */
	private short m_port;
	
	/**
	 * Preprocessors of connections.
	 */
	private List<EventBusAcceptPreprocessor> m_preprocessors;
	
	/**
	 * Creates a new event bus in the given port.
	 * @param port the port used to accept incoming clients.
	 * @param scope the primitive scope for types
	 * @throws IOException failed to open the server socket
	 */
	public EventBus(short port, PrimitiveScope scope) throws IOException {
		Ensure.isTrue(port > 0);
		Ensure.notNull(scope);
		m_group = new WorkerThreadGroup("Event Bus (" + port + ")");
		m_connections = new HashMap<>();
		m_accept_socket = new ServerSocket(port);
		m_accept_socket.setSoTimeout(SOCKET_TMEOUT_MS);
		m_group.add_thread(new CloseableWorkerThread<ServerSocket>(
				"Event bus (" + port + ") acceptor", m_accept_socket, true) {
			@Override
			protected void do_cycle_operation(ServerSocket closeable)
					throws Exception {
				accept_cycle(closeable);
			}
		});
		
		m_dispatcher = new LocalDispatcher<>();
		m_next_connection_id = 1;
		m_scope = scope;
		m_collector = new LocalCollector("Event bus (" + port + ")");
		m_port = port;
		m_preprocessors = new ArrayList<>();
	}
	
	/**
	 * Adds a new event bus accept preprocessor to the event bus.
	 * @param p the preprocessor
	 */
	public synchronized void add_preprocessor(EventBusAcceptPreprocessor p) {
		Ensure.not_null(p);
		m_preprocessors.add(p);
	}
	
	/**
	 * Removes a previously added event bus accept preprocessor from the
	 * event bus.
	 * @param p the preprocessor
	 */
	public synchronized void remove_preprocessor(EventBusAcceptPreprocessor p) {
		Ensure.not_null(p);
		boolean removed = m_preprocessors.remove(p);
		Ensure.is_true(removed);
	}
	
	/**
	 * Obtains the port where the event bus is listening.
	 * @return the port
	 */
	public synchronized short port() {
		return m_port;
	}
	
	/**
	 * Adds a new listener to the event bus.
	 * @param l the listener
	 */
	public synchronized void add_listener(EventBusListener l) {
		m_dispatcher.add(l);
	}
	
	/**
	 * Removes a previously registered listener from the event bus.
	 * @param l the listener
	 */
	public synchronized void remove_listener(EventBusListener l) {
		m_dispatcher.remove(l);
	}
	
	/**
	 * Perform an accept cycle: tries to accept a new connection.
	 * @param ss the server socket
	 * @throws IOException failed to accept the connection
	 */
	private void accept_cycle(ServerSocket ss) throws IOException {
		try {
			@SuppressWarnings("resource")
			Socket incoming = ss.accept();
			accept_connection(incoming);
		} catch (SocketTimeoutException e) {
			/*
			 * This one is OK. It means the accept has timed out and we can
			 * check whether it is time to leave or just cycle again.
			 */
		}
	}
	
	/**
	 * Accepts an incoming connection. This method will close the connection
	 * socket if the accept socket has been closed. That may happen as a
	 * result of the accept cycle not being synchronized.
	 * @param incoming the incoming connection
	 * @throws IOException failed to accept the connection
	 */
	private synchronized void accept_connection(Socket incoming)
			throws IOException {
		if (m_accept_socket == null) {
			incoming.close();
			return;
		}
		
		InetAddress addr = incoming.getInetAddress();
		final int id = m_next_connection_id;
		m_next_connection_id++;
		
		/*
		 * This actually *may* leak if the event bus connection data fails in 
		 * the constructor although this is not a testable situation.
		 */
		TextEncoding te = new DefaultTextEncoding();
		
		@SuppressWarnings("resource")
		DataTypeSocketConnection conn = new DataTypeSocketConnectionImpl(
				"Client " + id, incoming, te, m_scope);
		final BusDataQueue input_queue = new BusDataQueue();
		input_queue.dispatcher().add(new BusDataQueueListener() {
			@Override
			public void data_added_to_queue() {
				EventBus.this.received(input_queue, id);
			}
		});
		
		CloseableListener cl = new CloseableListener() {
			@Override
			public void closed(IOException e) {
				EventBus.this.closed(e, id);
			}
		};
		conn.closeable_dispatcher().add(cl);
		
		/*
		 * Just like the conn above.
		 */
		@SuppressWarnings("resource")
		ControlledDataTypeSocketConnectionImpl impl =
				new ControlledDataTypeSocketConnectionImpl(m_scope, conn);
		impl.queue_group().add(input_queue);
		final EventBusConnectionData data = new EventBusConnectionData(id, addr,
				impl, input_queue, cl);
		m_connections.put(id, data);
		
		/*
		 * Run all preprocessors on the connection.
		 */
		for (EventBusAcceptPreprocessor p : new ArrayList<>(m_preprocessors)) {
			if (!p.preprocess(impl)) {
				m_connections.remove(id);
				impl.stop();
				LOG.info("Connection with ID " + id + " from address "
						+ addr + " was rejected by pre-processor.");
				impl.close();
				conn.close();
				return;
			}
		}
		
		m_dispatcher.dispatch(new DispatcherOp<EventBusListener>() {
			@Override
			public void dispatch(EventBusListener l) {
				l.connection_accepted(data);
			}
		});
		
		impl.start();
		LOG.info("Accepted connection with ID " + id + " from address "
				+ addr + ".");
	}
	
	/**
	 * Invoked when a data value has been received from a connection.
	 * @param q the queue that may have received data
	 * @param id the connection ID
	 */
	private synchronized void received(final BusDataQueue q, int id) {
		final EventBusConnectionData connection = m_connections.get(id);
		
		/*
		 * It is possible that the connection no longer exists due to
		 * concurrency.
		 */
		if (connection == null) {
			return;
		}
		
		BusData v;
		while ((v = q.poll()) != null) {
			LOG.debug("Distributing from client " + id + ": " + v.value());
			
			connection.sent();
			
			for (EventBusConnectionData d : m_connections.values()) {
				d.received();
				try {
					d.connection().write(v);
				} catch (IOException e) {
					m_collector.collect(e, "Writing to client '" + d.id()
							+ "'.");
				}
			}
			
			final BusData vf = v;
			m_dispatcher.dispatch(new DispatcherOp<EventBusListener>() {
				@Override
				public void dispatch(EventBusListener l) {
					l.distributed(vf, connection);
				}
			});
		}
	}
	
	/**
	 * Invoked when a connection with a connection has been closed.
	 * @param e the exception, if any, that forced the connection
	 * @param id the client ID
	 */
	private synchronized void closed(IOException e, int id) {
		final EventBusConnectionData conn = m_connections.get(id);
		
		/*
		 * It is possible that the connection no longer exists due to
		 * concurrency.
		 */
		if (conn == null) {
			return;
		}
		
		m_connections.remove(id);
		conn.connection().stop();
		
		m_dispatcher.dispatch(new DispatcherOp<EventBusListener>() {
			@Override
			public void dispatch(EventBusListener l) {
				l.connection_disconnected(conn);
			}
		});
		
		LOG.info("Client " + id + " disconnected (e = " + e + ").");
	}

	@Override
	public synchronized void close() throws IOException {
		if (m_accept_socket == null) {
			/*
			 * Already closed. We need to support multiple closes because
			 * that's the contract of Closeable.
			 */
			return;
		}
		
		ExceptionSuppress<IOException> ex = new ExceptionSuppress<>();
		
		/*
		 * We need to close the server socket first otherwise clients may
		 * connect while we're disconnecting them.
		 */
		try {
			m_accept_socket.close();
		} catch (IOException e) {
			ex.add(e);
		}
		
		for (EventBusConnectionData c : m_connections.values()) {
			try {
				c.connection().close();
			} catch (IOException e) {
				ex.add(e);
			}
		}
		
		LOG.info("Closing event bus.");
		
		m_group.stop_all();
		m_accept_socket = null;
		ex.maybe_throw();
	}
	
	/**
	 * Obtains the worker thread group.
	 * @return the worker thread group
	 */
	public WorkerThreadGroupCI thread_group() {
		return m_group;
	}
	
	/**
	 * Starts the event bus.
	 */
	public void start() {
		Ensure.not_null(m_accept_socket);
		
		LOG.info("Starting event bus.");
		m_group.start();
	}
	
	/**
	 * Checks whether the event bus has been closed.
	 * @return has the event bus been closed?
	 */
	public synchronized boolean closed() {
		return m_accept_socket == null;
	}
	
	/**
	 * Obtains the incoming event chain for a client.
	 * @param client_id the client ID
	 * @return the incoming event chain, <code>null</code> if the client
	 * is not found
	 */
	public synchronized EventFilterChain incoming_chain(int client_id) {
		EventBusConnectionData cdata = m_connections.get(client_id);
		if (cdata == null) {
			return null;
		}
		
		return cdata.connection().incoming_chain();
	}
	
	/**
	 * Obtains the outgoing event chain for a client.
	 * @param client_id the client ID
	 * @return the outgoing event chain, <code>null</code> if the client
	 * is not found
	 */
	public synchronized EventFilterChain outgoing_chain(int client_id) {
		EventBusConnectionData cdata = m_connections.get(client_id);
		if (cdata == null) {
			return null;
		}
		
		return cdata.connection().outgoing_chain();
	}
}
