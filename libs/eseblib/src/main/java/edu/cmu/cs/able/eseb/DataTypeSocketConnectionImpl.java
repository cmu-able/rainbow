package edu.cmu.cs.able.eseb;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;
import incubator.wt.CloseableListener;
import incubator.wt.CloseableWorkerThreadGroupOps;
import incubator.wt.WorkerThreadGroup;
import incubator.wt.WorkerThreadGroupCI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Implementation of a {@link DataTypeSocketConnection}.
 */
public class DataTypeSocketConnectionImpl implements DataTypeSocketConnection {
	/**
	 * Milliseconds between socket read timeouts.
	 */
	private static final int SOCKET_TIMEOUT_MS = 50;
	
	/**
	 * Thread group with both the input, output and pinger threads.
	 */
	private WorkerThreadGroup m_group;
	
	/**
	 * The input thread.
	 */
	private DataTypeInputThread m_input;
	
	/**
	 * The output thread.
	 */
	private DataTypeOutputThread m_output;
	
	/**
	 * The socket or <code>null</code> if it has been closed.
	 */
	private Socket m_socket;
	
	/**
	 * Closeable listeners.
	 */
	private LocalDispatcher<CloseableListener> m_dispatcher;
	
	/**
	 * Creates a new connection.
	 * @param name the name of the connection
	 * @param s the socket
	 * @param enc the encoding to use
	 * @param scope the primitive type scope to use
	 * @throws IOException failed to obtain the streams from the socket
	 */
	public DataTypeSocketConnectionImpl(String name, Socket s,
			DataValueEncoding enc, PrimitiveScope scope) throws IOException {
		Ensure.not_null(name);
		Ensure.not_null(s);
		Ensure.not_null(enc);
		Ensure.not_null(scope);
		
		m_group = new WorkerThreadGroup(name);
		m_socket = s;
		m_dispatcher = new LocalDispatcher<>();
		
		@SuppressWarnings("resource")
		InputStream is = s.getInputStream();
		Ensure.not_null(is);
		@SuppressWarnings("resource")
		OutputStream os = s.getOutputStream();
		Ensure.not_null(os);
		
		@SuppressWarnings("resource")
		DataTypeInputStream dtis = new DataTypeInputStreamImpl(is, enc, scope);
		@SuppressWarnings("resource")
		DataTypeOutputStream dtos = new DataTypeOutputStreamImpl(os, enc);
		
		/*
		 * Set the time we wait for socket timeouts.
		 * 
		 * This is immensely tricky.
		 * The trickiness of the following code can never be fully understood.
		 * 
		 * Socket reads are not necessarily interruptible. It is undefined.
		 * So, we need to figure out a way to be able to interrupt the read
		 * which is done by setting a timeout. However, this will generate an
		 * I/O exception which, in turn, would close the socket. So we override
		 * handle_exception and tell the worker thread that we're still happy
		 * and we want it to continue cycling.
		 */
		m_socket.setSoTimeout(SOCKET_TIMEOUT_MS);
		
		m_input = new DataTypeInputThread(name + " (input)", dtis) {
			@Override
			protected synchronized boolean handle_failure(Throwable t) {
				if (t instanceof SocketTimeoutException) {
					return false;
				}
				
				return super.handle_failure(t);
			}
		};
		
		m_output = new DataTypeOutputThread(name + " (output)", dtos);
		
		m_group.add_thread(m_input);
		m_group.add_thread(m_output);
		
		m_input.add_listener(new CloseableListener() {
			@Override
			public void closed(IOException e) {
				DataTypeSocketConnectionImpl.this.closed(e);
			}
		});
		
		m_output.add_listener(new CloseableListener() {
			@Override
			public void closed(IOException e) {
				DataTypeSocketConnectionImpl.this.closed(e);
			}
		});
	}

	@Override
	public void write(DataValue dt) throws IOException {
		Ensure.not_null(dt, "dt == null");
		m_output.write(dt);
	}

	@Override
	public void write(BusData bd) throws IOException {
		Ensure.not_null(bd, "bd == null");
		m_output.write(bd);
	}

	@Override
	public void close() throws IOException {
		Socket s;
		
		synchronized (this) {
			/*
			 * Closing the streams will invoke the closed method so we'll make
			 * sure socket is null to let it know the socket has already been
			 * closed.
			 */
			if (m_socket == null) {
				return;
			}
			
			s = m_socket;
			m_socket = null;
		}
		
		try {
			s.close();
			CloseableWorkerThreadGroupOps.close_all(m_group);
		} finally {
			m_dispatcher.dispatch(new DispatcherOp<CloseableListener>() {
				@Override
				public void dispatch(CloseableListener l) {
					l.closed(null);
				}
			});
		}
	}
	
	/**
	 * Invoked when either of the streams is closed.
	 * @param ex the exception that made the stream close, if any
	 */
	private void closed(final IOException ex) {
		Socket s = null;
		
		synchronized (this) {
			/*
			 * Because this method may be invoked several times, we set the
			 * socket to null to make sure we detect further invocations.
			 */
			if (m_socket == null) {
				return;
			}
			
			s = m_socket;
			m_socket = null;
		}
		
		try {
			s.close();
			CloseableWorkerThreadGroupOps.close_all(m_group);
		} catch (IOException e) {
			/*
			 * Slippery terrain here. We know closed has been already
			 * invoked (because we're in this method) but now closing the
			 * socket has failed (or closing some of the streams).
			 * 
			 * The problem here is that the close that triggered this
			 * exception may have succeeded but we don't and now we can't
			 * throw an exception.
			 * 
			 * We'll ignore this as it is really difficult to deal with this
			 * and, after all, closing the socket or any of its streams is
			 * kind of equivalent according to the javadoc.
			 */
		}
		
		m_dispatcher.dispatch(new DispatcherOp<CloseableListener>() {
			@Override
			public void dispatch(CloseableListener l) {
				l.closed(ex);
			}
		});
	}
	
	@Override
	public void start() {
		m_group.start();
	}
	
	@Override
	public void stop() {
		m_group.stop();
	}
	
	@Override
	public WorkerThreadGroupCI thread_group() {
		return m_group;
	}

	@Override
	public BusDataQueueGroup queue_group() {
		return m_input.queue_group();
	}

	@Override
	public Dispatcher<CloseableListener> closeable_dispatcher() {
		return m_dispatcher;
	}
}
