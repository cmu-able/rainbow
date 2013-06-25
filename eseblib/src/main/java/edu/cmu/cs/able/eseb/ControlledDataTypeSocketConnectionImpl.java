package edu.cmu.cs.able.eseb;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;
import incubator.wt.CloseableListener;
import incubator.wt.CloseableWorkerThread;
import incubator.wt.WorkerThread;
import incubator.wt.WtState;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Implementation of a controlled socket connection which works on top of
 * a {@link DataTypeSocketConnection}.
 */
class ControlledDataTypeSocketConnectionImpl
		implements ControlledDataTypeSocketConnection {
	/**
	 * Logger to use.
	 */
	private static final Logger LOG = Logger.getLogger(
			ControlledDataTypeSocketConnectionImpl.class);
	
	/**
	 * Prefix for commands send through the bus.
	 */
	static final String CMD_PREFIX = "esebctl:";

	/**
	 * Command with a ping.
	 */
	static final String CMD_PING = "ping";
	
	/**
	 * Command stating that no data is to be sent to the connection.
	 */
	static final String CMD_NOSEND = "nosend";
	
	/**
	 * Default interval to check for pings.
	 */
	private static long PING_CHECK_INTERVAL_MS = 250;
	
	/**
	 * Default interval between ping sends.
	 */
	private static long PING_SEND_INTERVAL_MS = 1000;
	
	/**
	 * Default interval after which the connection is closed if no pings
	 * have been received.
	 */
	private static long PING_MAX_INTERVAL_MS = 2500;
	
	/**
	 * The connection.
	 */
	private DataTypeSocketConnection m_connection;
	
	/**
	 * Dispatcher of closeable events.
	 */
	private LocalDispatcher<CloseableListener> m_closeable_dispatcher;
	
	/**
	 * Queue group that is informed when data is received.
	 */
	private BusDataQueueGroupImpl m_queue_group;
	
	/**
	 * Should data be sent through this connection?
	 */
	private boolean m_send_data;
	
	/**
	 * Has the connection been closed?
	 */
	private boolean m_closed;
	
	/**
	 * Timestamp when the last ping was received (obtained through
	 * <code>System.currentTimeMillis()</code>.
	 */
	private long m_last_received_ping;
	
	/**
	 * Timestamp when the last ping was sent (obtained through
	 * <code>System.currentTimeMillis()</code>.
	 */
	private long m_last_sent_ping;
	
	/**
	 * Pinger thread which will only be running if the connection is
	 * active.
	 */
	private WorkerThread m_pinger;
	
	/**
	 * The primitive data scope.
	 */
	private PrimitiveScope m_primitive_scope;
	
	/**
	 * Queue used to receive bus data.
	 */
	private BusDataQueue m_receive_queue;
	
	/**
	 * Interval to check for pings. Will be equal to
	 * {@link #PING_CHECK_INTERVAL_MS} unless set otherwise (usually by unit
	 * tests).
	 */
	long m_ping_check_interval_ms;
	
	/**
	 * Interval between pings sent. Will be equal to
	 * {@link #PING_SEND_INTERVAL_MS} unless set otherwise (usually by unit
	 * tests).
	 */
	long m_ping_send_interval_ms;
	
	/**
	 * Maximum interval between received pings after which the connection will
	 * be closed. Will be equal to {@link #PING_MAX_INTERVAL_MS} unless
	 * set otherwise (usually by unit tests). 
	 */
	long m_ping_max_interval_ms;
	
	/**
	 * Creates a new connection.
	 * @param primitive_scope the primitive data type scope
	 * @param conn the connection
	 */
	ControlledDataTypeSocketConnectionImpl(PrimitiveScope primitive_scope,
			DataTypeSocketConnection conn) {
		Ensure.notNull(primitive_scope);
		Ensure.notNull(conn);
		m_primitive_scope = primitive_scope;
		m_connection = conn;
		m_closeable_dispatcher = new LocalDispatcher<>();
		m_queue_group = new BusDataQueueGroupImpl();
		m_send_data = true;
		m_closed = false;
		m_receive_queue = new BusDataQueue();
		m_last_received_ping = 0;
		m_last_sent_ping = 0;
		m_ping_check_interval_ms = PING_CHECK_INTERVAL_MS;
		m_ping_send_interval_ms = PING_SEND_INTERVAL_MS;
		m_ping_max_interval_ms = PING_MAX_INTERVAL_MS;
		m_pinger = new CloseableWorkerThread<DataTypeSocketConnection>(
				"Ping Control (" + conn.thread_group().name() + ")", conn,
				true) {
			@Override
			protected void do_cycle_operation(
					DataTypeSocketConnection closeable)
					throws Exception {
				handle_pings();
			}
		};
		
		
		m_connection.closeable_dispatcher().add(new CloseableListener() {
			@Override
			public void closed(IOException e) {
				connection_closed(e);
			}
		});
		
		m_connection.queue_group().add(m_receive_queue);
		m_receive_queue.dispatcher().add(new BusDataQueueListener() {
			@Override
			public void data_added_to_queue() {
				bus_data_received();
			}
		});
	}
	
	@Override
	public Dispatcher<CloseableListener> closeable_dispatcher() {
		return m_closeable_dispatcher;
	}
	
	@Override
	public synchronized BusDataQueueGroup queue_group() {
		return m_queue_group;
	}
	
	/**
	 * Invoked when the connection has been closed.
	 * @param e the exception that closed the connection
	 */
	private synchronized void connection_closed(final IOException e) {
		m_closeable_dispatcher.dispatch(new DispatcherOp<CloseableListener>() {
			@Override
			public void dispatch(CloseableListener l) {
				l.closed(e);
			}
		});
		
		m_closed = true;
		
		m_closeable_dispatcher.dispatch(new Runnable() {
			@Override
			public void run() {
				stop_pinger_if_running();
			}
		});
	}
	
	/**
	 * Invoked when data has been received from the event bus and placed in
	 * the queue.
	 */
	private synchronized void bus_data_received() {
		BusData bd;
		
		synchronized(m_receive_queue) {
			while ((bd = m_receive_queue.poll()) != null) {
				if (bd.value() instanceof StringValue) {
					String text = ((StringValue) bd.value()).value();
					if (text.startsWith(CMD_PREFIX)) {
						process_cmd(text.substring(CMD_PREFIX.length()));
						return;
					}
				}
				
				m_queue_group.add(bd);
			}
		}
	}
	
	/**
	 * Processes a command.
	 * @param cmd the command, with the prefix removed.
	 */
	private synchronized void process_cmd(String cmd) {
		Ensure.not_null(cmd);
		switch (cmd) {
		case CMD_PING:
			m_last_received_ping = System.currentTimeMillis();
			break;
		case CMD_NOSEND:
			m_send_data = false;
			break;
		default:
			/*
			 * Unknown command, ignored.
			 */
		}
	}

	@Override
	public void write(DataValue v) throws IOException {
		Ensure.not_null(v);
		write(new BusData(v));
	}

	@Override
	public void write(BusData bd) throws IOException {
		Ensure.not_null(bd);
		if (!m_closed && m_send_data) {
			m_connection.write(bd);
		}
	}
	
	/**
	 * Checks if there is any work to do related to pings. This method is
	 * invoked in a cycle from a worker thread
	 * @throws Exception failed to handle pings
	 */
	private synchronized void handle_pings() throws Exception {
		long now = System.currentTimeMillis();
		
		if (m_last_received_ping < (now - m_ping_max_interval_ms)) {
			String msg = "No heartbeat received in the last "
					+ (now - m_last_received_ping) + "ms.";
			LOG.info(msg + " Closing connection.");
			throw new IOException(msg);
		}
		
		if (m_last_sent_ping < (now - m_ping_send_interval_ms)) {
			if (!m_closed) {
				m_connection.write(m_primitive_scope.string().make(CMD_PREFIX
						+ CMD_PING));
				m_last_sent_ping = now;
			}
		}
		
		wait(m_ping_check_interval_ms);
	}
	
	@Override
	public void start() {
		long now = System.currentTimeMillis();
		m_last_received_ping = now;
		m_last_sent_ping = now;
		
		m_connection.start();
		m_pinger.start();
	}
	
	@Override
	public void stop() {
		m_connection.stop();
		stop_pinger_if_running();
	}

	@Override
	public void publish_only() throws IOException {
		write(m_primitive_scope.string().make(CMD_PREFIX + CMD_NOSEND));
	}
	
	@Override
	public void close() throws IOException {
		m_connection.close();
		stop_pinger_if_running();
	}
	
	/**
	 * Stops the pinger thread if it is running. This method should not be
	 * invoked with the lock on the current object (otherwise the pinger
	 * cannot exit).
	 */
	private void stop_pinger_if_running() {
		synchronized (m_pinger) {
			if (m_pinger.state() == WtState.RUNNING) {
				m_pinger.stop();
			}
		}
	}
}
