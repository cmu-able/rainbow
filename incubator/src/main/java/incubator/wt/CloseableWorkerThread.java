package incubator.wt;

import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;

import java.io.Closeable;
import java.io.IOException;

/**
 * A closeable worker thread is a worker thread that operates on a closeable
 * object. The worker thread has some specificities: the cycle operation
 * will revert to the default if the closeable object has been closed. If
 * the cycle method throws an {@link IOException}, the closeable will be
 * closed automatically. Listeners can be attached which will be informed
 * when the closeable has been closed.
 * @param <T> the type of closeable object
 */
public abstract class CloseableWorkerThread<T extends Closeable>
		extends WorkerThread implements Closeable {
	/**
	 * The closeable object or <code>null</code> if the object has already
	 * been closed.
	 */
	private T m_closeable;
	
	/**
	 * Should the closeable be closed on abort?
	 */
	private boolean m_close_on_abort;
	
	/**
	 * The dispatcher.
	 */
	private LocalDispatcher<CloseableListener> m_dispatcher;
	
	/**
	 * Creates a new worker thread.
	 * @param name the thread name
	 * @param closeable the closeable to work on
	 * @param close_on_abort if the thread aborts, should the closeable be
	 * closed?
	 */
	public CloseableWorkerThread(String name, T closeable,
			boolean close_on_abort) {
		super(name);
		
		Ensure.notNull(closeable);
		m_closeable = closeable;
		m_dispatcher = new LocalDispatcher<>();
		m_close_on_abort = close_on_abort;
	}
	
	/**
	 * Adds a new listner that will be informed when the closeable is closed.
	 * @param l the listener
	 */
	public synchronized void add_listener(CloseableListener l) {
		m_dispatcher.add(l);
	}
	
	/**
	 * Removes a previously registered listener.
	 * @param l the listener
	 */
	public synchronized void remove_listener(CloseableListener l) {
		m_dispatcher.remove(l);
	}
	
	@Override
	public synchronized void close() throws IOException {
		if (m_closeable == null) {
			return;
		}
		
		T t = m_closeable;
		m_closeable = null;
		
		/*
		 * Some closeable classes like PipedInputStream will block forever
		 * in a read() method even if we close the stream. Therefore we need
		 * to interrupt the thread. Note that at least PipedInputStream
		 * will fail to read if the stream is closed so there may not be a
		 * racing condition here.
		 */
		if (state() == WtState.RUNNING) {
			thread().interrupt();
		}
		
		try {
			t.close();
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
	 * Checks whether the closeable has been closed or not.
	 * @return has it been closed?
	 */
	public synchronized boolean closed() {
		return m_closeable == null;
	}

	@Override
	protected final void do_cycle_operation() throws Exception {
		T closeable = null;
		synchronized (this) {
			closeable = m_closeable;
			if (closeable == null) {
				wait();
			}
		}
		
		if (closeable != null) {
			do_cycle_operation(closeable);
		}
	}
	
	/**
	 * Performs the cycle operation. The closeable is known not to be closed
	 * when this method is invoked but because this method runs without locks
	 * it may be closed during execution.
	 * @param closeable the closeable to operate on which is guaranteed to
	 * be not <code>null</code>
	 * @throws InterruptedException will be ignored
	 * @throws IOException will trigger the closeable to be closed
	 * @throws Exception execution failed
	 */
	protected abstract void do_cycle_operation(T closeable) throws Exception;

	@Override
	protected final void interrupt_wait() {
		thread().interrupt();
	}

	/**
	 * This implementation will close the closeable if (1) the exception is
	 * a <code>IOExceptin</code> or if (2) <code>close_on_abort</code> was
	 * specified in the constructor. This implementation will always return
	 * <code>true</code>.
	 */
	@Override
	protected synchronized boolean handle_failure(final Throwable t) {
		/*
		 * We call the super implementation to collect the throwable.
		 */
		super.handle_failure(t);
		
		final IOException io_exception;
		if (t instanceof IOException) {
			io_exception = (IOException) t;
		} else if (m_close_on_abort) {
			io_exception = new IOException("I/O closed due to exception.", t);
		} else {
			io_exception = null;
		}
		
		if (io_exception != null) {
			/*
			 * If m_closeable is null it means that the closeable has
			 * already been closed and we will ignore the exception but
			 * will still abort the thread.
			 */
			if (m_closeable != null) {
				T closeable = m_closeable;
				m_closeable = null;
				try {
					closeable.close();
				} catch (IOException e) {
					super.handle_failure(e);
				}
				
				m_dispatcher.dispatch(
						new DispatcherOp<CloseableListener>() {
					@Override
					public void dispatch(CloseableListener l) {
						l.closed(io_exception);
					}
				});
			}
		}
		
		return true;
	}
}
