package edu.cmu.cs.able.eseb;

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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;

/**
 * Implementation of an event bus server which opens a port and receives
 * clients in the port. All messages sent by one client are forward to all
 * other clients.
 */
public class BusServer implements Closeable {
	/**
	 * Logger to use.
	 */
	private static final Logger LOG = Logger.getLogger(BusServer.class);
	
	/**
	 * How much time to wait for the socket to time out.
	 */
	private static final int SOCKET_TMEOUT_MS = 100;
	
	/**
	 * Group with all bus server worker threads.
	 */
	private WorkerThreadGroup m_group;
	
	/**
	 * Maps client IDs with their connections.
	 */
	private Map<Integer, BusServerClientData> m_clients;
	
	/**
	 * The socket where client connections are received. It will be
	 * <code>null</code> when shut down.
	 */
	private ServerSocket m_server;
	
	/**
	 * Dispatcher.
	 */
	private LocalDispatcher<BusServerListener> m_dispatcher;
	
	/**
	 * The next ID for a client.
	 */
	private int m_next_client_id;
	
	/**
	 * The primitive scope for server types.
	 */
	private PrimitiveScope m_scope;
	
	/**
	 * Exception collector.
	 */
	private LocalCollector m_collector;
	
	/**
	 * The port where the server is running.
	 */
	private short m_port;
	
	/**
	 * Creates a new bus server in the given port.
	 * @param port the port used to accept incoming clients.
	 * @param scope the primitive scope for types
	 * @throws IOException failed to open the server socket
	 */
	public BusServer(short port, PrimitiveScope scope) throws IOException {
		Ensure.isTrue(port > 0);
		Ensure.notNull(scope);
		m_group = new WorkerThreadGroup("Bus Server (" + port + ")");
		m_clients = new HashMap<>();
		m_server = new ServerSocket(port);
		m_server.setSoTimeout(SOCKET_TMEOUT_MS);
		m_group.add_thread(new CloseableWorkerThread<ServerSocket>(
				"Bus Server (" + port + ") acceptor", m_server, true) {
			@Override
			protected void do_cycle_operation(ServerSocket closeable)
					throws Exception {
				accept_cycle(closeable);
			}
		});
		
		m_dispatcher = new LocalDispatcher<>();
		m_next_client_id = 1;
		m_scope = scope;
		m_collector = new LocalCollector("Bus server (" + port + ")");
		m_port = port;
	}
	
	/**
	 * Obtains the port where the server is listening.
	 * @return the port
	 */
	public short port() {
		return m_port;
	}
	
	/**
	 * Adds a new listener to the bus server.
	 * @param l the listener
	 */
	public synchronized void add_listener(BusServerListener l) {
		m_dispatcher.add(l);
	}
	
	/**
	 * Removes a previously registered listener from the bus server.
	 * @param l the listener
	 */
	public synchronized void remove_listener(BusServerListener l) {
		m_dispatcher.remove(l);
	}
	
	/**
	 * Perform an accept cycle: tries to accept a new client.
	 * @param ss the server socket
	 * @throws IOException failed to accept the client
	 */
	private void accept_cycle(ServerSocket ss) throws IOException {
		try {
			@SuppressWarnings("resource")
			Socket incoming = ss.accept();
			accept_client(incoming);
		} catch (SocketTimeoutException e) {
			/*
			 * This one is OK. It means the accept has timed out and we can
			 * check whether it is time to leave or just cycle again.
			 */
		}
	}
	
	/**
	 * Accepts an incoming client. This method will close the client socket
	 * if the server socket has been closed. That may happen as a result of
	 * the accept cycle not being synchronized.
	 * @param incoming the incoming client
	 * @throws IOException failed to accept the client
	 */
	private synchronized void accept_client(Socket incoming)
			throws IOException {
		if (m_server == null) {
			incoming.close();
			return;
		}
		
		InetAddress addr = incoming.getInetAddress();
		final int id = m_next_client_id;
		m_next_client_id++;
		
		/*
		 * This actually *may* leak if the bus server client data fails in 
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
				BusServer.this.received(input_queue, id);
			}
		});
		
		CloseableListener cl = new CloseableListener() {
			@Override
			public void closed(IOException e) {
				BusServer.this.closed(e, id);
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
		final BusServerClientData data = new BusServerClientData(id, addr,
				impl, input_queue, cl);
		m_clients.put(id, data);
		m_dispatcher.dispatch(new DispatcherOp<BusServerListener>() {
			@Override
			public void dispatch(BusServerListener l) {
				l.client_accepted(data);
			}
		});
		
		impl.start();
		LOG.info("Accepted client with ID " + id + " from address "
				+ addr + ".");
	}
	
	/**
	 * Invoked when a data value has been received from a client.
	 * @param q the queue that may have received data
	 * @param id the client ID
	 */
	private synchronized void received(final BusDataQueue q, int id) {
		final BusServerClientData client = m_clients.get(id);
		
		/*
		 * It is possible that the client no longer exists due to
		 * concurrency.
		 */
		if (client == null) {
			return;
		}
		
		BusData v;
		while ((v = q.poll()) != null) {
			LOG.debug("Distributing from client " + id + ": " + v.value());
			
			client.sent();
			
			for (BusServerClientData d : m_clients.values()) {
				d.received();
				try {
					d.connection().write(v);
				} catch (IOException e) {
					m_collector.collect(e, "Writing to client '" + d.id()
							+ "'.");
				}
			}
			
			final BusData vf = v;
			m_dispatcher.dispatch(new DispatcherOp<BusServerListener>() {
				@Override
				public void dispatch(BusServerListener l) {
					l.distributed(vf, client);
				}
			});
		}
	}
	
	/**
	 * Invoked when a connection with a client has been closed.
	 * @param e the exception, if any, that forced the connection
	 * @param id the client ID
	 */
	private synchronized void closed(IOException e, int id) {
		final BusServerClientData client = m_clients.get(id);
		
		/*
		 * It is possible that the client no longer exists due to
		 * concurrency.
		 */
		if (client == null) {
			return;
		}
		
		m_clients.remove(id);
		client.connection().stop();
		
		m_dispatcher.dispatch(new DispatcherOp<BusServerListener>() {
			@Override
			public void dispatch(BusServerListener l) {
				l.client_disconnected(client);
			}
		});
		
		LOG.info("Client " + id + " disconnected (e = " + e + ").");
	}

	@Override
	public synchronized void close() throws IOException {
		if (m_server == null) {
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
			m_server.close();
		} catch (IOException e) {
			ex.add(e);
		}
		
		for (BusServerClientData c : m_clients.values()) {
			try {
				c.connection().close();
			} catch (IOException e) {
				ex.add(e);
			}
		}
		
		LOG.info("Closing bus server.");
		
		m_group.stop_all();
		m_server = null;
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
	 * Starts the bus server.
	 */
	public void start() {
		Ensure.not_null(m_server);
		
		LOG.info("Starting bus server.");
		m_group.start();
	}
	
	/**
	 * Checks whether the server has been closed.
	 * @return has the server been closed?
	 */
	public synchronized boolean closed() {
		return m_server == null;
	}
}
