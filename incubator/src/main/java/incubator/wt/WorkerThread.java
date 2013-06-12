package incubator.wt;

import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.exh.LocalCollector;
import incubator.exh.ThrowableCollector;
import incubator.pval.Ensure;

/**
 * A worker thread is a thread which implements a specific pattern: a
 * task is done in a loop which may contain a waiting primitive. The waiting
 * primitive is assumed to be interruptible and it is assumed it does not hold
 * the lock of the object while it is waiting. This
 * superclass provides a common framework to implement these types of threads.
 */
public class WorkerThread implements WorkerThreadCI {
	/**
	 * How much time to wait between polls for a thread state change. 
	 */
	private static final long THREAD_STATE_POLL_TIME_MS = 50;
	
	/**
	 * The thread's name.
	 */
	private String m_name;
	
	/**
	 * State of the thread.
	 */
	private WtState m_state;
	
	/**
	 * Description of the thread.
	 */
	private String m_description;
	
	/**
	 * The actual thread. This will be <code>null</code> when the thread is
	 * stopped and not <code>null</code> when it is running. It will also be
	 * <code>null</code> if the thread has been aborted.
	 */
	private Thread m_thread;
	
	/**
	 * Collector of throwables thrown by the thread.
	 */
	private ThrowableCollector m_collector;
	
	/**
	 * The local dispatcher.
	 */
	private LocalDispatcher<WorkerThreadListener> m_dispatcher;
	
	/**
	 * Creates a new worker thread.
	 * @param name the thread name
	 */
	public WorkerThread(String name) {
		Ensure.notNull(name);
		m_name = name;
		m_state = WtState.STOPPED;
		m_description = null;
		m_thread = null;
		m_collector = new LocalCollector("Worker thread: " + name);
		m_dispatcher = new LocalDispatcher<>();
	}
	
	/**
	 * Adds a worker thread listener.
	 * @param l the listener
	 */
	public void add_listener(WorkerThreadListener l) {
		m_dispatcher.add(l);
	}
	
	/**
	 * Removes a worker thread listener.
	 * @param l the listener
	 */
	public void remove_listener(WorkerThreadListener l) {
		m_dispatcher.remove(l);
	}
	
	@Override
	public String name() {
		return m_name;
	}
	
	@Override
	public synchronized String description() {
		return m_description;
	}

	@Override
	public synchronized ThrowableCollector collector() {
		return m_collector;
	}
	
	/**
	 * Defines what the description of the thread should be.
	 * @param description the description
	 */
	protected synchronized void description(String description) {
		m_description = description;
	}
	
	@Override
	public final synchronized WtState state() {
		return m_state;
	}
	
	@Override
	public final synchronized void start() {
		Ensure.stateCondition(m_state == WtState.STOPPED
				|| m_state == WtState.ABORTED);
		assert m_thread == null;
		m_state = WtState.STARTING;
		
		fire_state_changed();
		
		m_thread = new Thread(m_name) {
			@Override
			public void run() {
				WorkerThread.this.run();
			}
		};
		
		m_thread.start();

		/*
		 * The first condition is to support the possibility that the thread
		 * may abort immediately on start. It will set m_thread to null
		 * in its execution.
		 */
		while (m_thread != null && !m_thread.isAlive()) {
			try {
				wait(THREAD_STATE_POLL_TIME_MS);
			} catch (InterruptedException e) {
				/*
				 * We'll just cycle again faster.
				 */
			}
		}
		
		if (m_thread != null) {
			m_state = WtState.RUNNING;
			fire_state_changed();
		} else {
			assert m_state == WtState.ABORTED;
		}
	}
	
	@Override
	public final synchronized void stop() {
		Ensure.stateCondition(m_state == WtState.RUNNING);
		
		if (Thread.currentThread() == m_thread) {
			throw new ClosingWorkerThreadFromWithinException();
		}
		
		m_state = WtState.STOPPING;
		fire_state_changed();
		
		/*
		 * We need to save t as the thread may abort during wait...
		 */
		Thread t = m_thread;
		while (t.isAlive()) {
			try {
				interrupt_wait();
				wait(THREAD_STATE_POLL_TIME_MS);
			} catch (InterruptedException e) {
				/*
				 * We'll just cycle again faster.
				 */
			}
		}
		
		if (m_thread != null) {
			m_state = WtState.STOPPED;
			fire_state_changed();
			m_thread = null;
		} else {
			assert m_state == WtState.ABORTED;
		}
	}
	
	/**
	 * Method that performs the thread work. This method may not be
	 * overridden. This method will keep running
	 * in a loop invoking, {@link #do_cycle_operation()}.
	 * If the methods throws an exception,
	 * {@link #handle_failure(Throwable)} if invoked to handle it.
	 */
	protected final void run() {
		while (true) {
			synchronized (this) {
				if (m_state == WtState.STOPPING) {
					/*
					 * We want to shut down.
					 */
					break;
				}
			}
			
			try {
				do_cycle_operation();
			} catch (InterruptedException e) {
				/*
				 * Ok, we'll just loop again.
				 */
			} catch (ClosingWorkerThreadFromWithinException e) {
				/*
				 * We want to stop, so we'll stop.
				 */
				synchronized(this) {
					m_state = WtState.STOPPED;
					fire_state_changed();
					m_thread = null;
					break;
				}
			} catch (Throwable t) {
				if (handle_failure(t)) {
					synchronized(this) {
						/*
						 * Aborting thread...
						 */
						m_state = WtState.ABORTED;
						fire_state_changed();
						m_thread = null;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Performs the worker thread's wait operation. The default implementation
	 * performs a {@link #wait()} after acquiring the synchronization lock.
	 * @throws Exception wait failed; if {@link InterruptedException} is
	 * thrown, it will be ignored. All other exceptions will be handled by
	 * {@link #handle_failure(Throwable)}
	 */
	protected void do_cycle_operation() throws Exception {
		synchronized (this) {
			wait();
		}
	}
	
	/**
	 * This method is invoked when it necessary to interrupt the slow
	 * operation although, when invoked, the wait operation may not be
	 * running due to concurrency issues. Therefore, this method may end up
	 * being invoked multiple times. The default implementation performs a
	 * {@link #notifyAll()} after acquiring the synchronization lock on the
	 * object.
	 */
	protected void interrupt_wait() {
		synchronized (this) {
			notifyAll();
		}
	}
	
	/**
	 * Invoked when {@link #do_cycle_operation()} throws
	 * something other than {@link InterruptedException}. The default
	 * implementation will collect the throwable in the collector obtained
	 * by {@link #collector()}. Sub classes may override this method to
	 * provide their own exception handling.
	 * @param t the throwable
	 * @return should the thread be aborted? The default implementation
	 * returns <code>true</code>
	 */
	protected boolean handle_failure(Throwable t) {
		synchronized (this) {
			m_collector.collect(t, name());
		}
		
		return true;
	}
	
	/**
	 * Obtains the thread object.
	 * @return the thread or <code>null</code> if it is not running or it has
	 * aborted
	 */
	protected Thread thread() {
		return m_thread;
	}
	
	/**
	 * Fires an event informing listeners that the thread state has changed.
	 */
	private void fire_state_changed() {
		m_dispatcher.dispatch(new DispatcherOp<WorkerThreadListener>() {
			@Override
			public void dispatch(WorkerThreadListener l) {
				l.state_changed();
			}
		});
	}
}
