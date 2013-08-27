package incubator.dispatch;

import incubator.exh.LocalCollector;
import incubator.pval.Ensure;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Global event dispatcher.
 */
public class GlobalDispatcher {
	/**
	 * Default core pool size.
	 */
	private static final int DEFAULT_CORE_POOL_SIZE = 5;
	
	/**
	 * Thread inactivity timeout.
	 */
	private static final int INACTIVITY_TIMEOUT_MS = 30_000;
	
	/**
	 * Time to wait between polls to check whether threads have terminated
	 * or not.
	 */
	private static final long TERMINATION_POLLING_WAIT_TIME_MS = 50;
	
	/**
	 * The global instance.
	 */
	private static GlobalDispatcher m_instance;
	
	/**
	 * The executor pool.
	 */
	private ThreadPoolExecutor m_executor;
	
	/**
	 * Number of tasks waiting to be executed.
	 */
	private int m_waiting;
	
	/**
	 * Number of tasks waiting to be executed.
	 */
	private int m_executing;
	
	/**
	 * Collects exceptions during dispatch.
	 */
	private LocalCollector m_collector;
	
	/**
	 * Creates a new dispatcher. If timeout is <code>false</code> then the
	 * dispatcher also pre-starts all threads which is useful for unit testing
	 * if we use thread leak detection as the number of threads started at
	 * boot and stopped at shutdown is fixed.
	 * @param timeout_threads should threads timeout if inactive?
	 */
	private GlobalDispatcher(boolean timeout_threads) {
		m_executor = new ThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE,
				DEFAULT_CORE_POOL_SIZE, INACTIVITY_TIMEOUT_MS,
				TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		
		if (timeout_threads) {
			m_executor.allowCoreThreadTimeOut(true);
		} else {
			m_executor.prestartAllCoreThreads();
		}
		
		m_waiting = 0;
		m_executing = 0;
		
		/*
		 * We can't set the local collector here because the collector also
		 * uses the global dispatcher...
		 */
		m_collector = null;
	}
	
	/**
	 * Obtains the singleton instance of the global dispatcher.
	 * @return the global dispatcher
	 */
	public static synchronized GlobalDispatcher instance() {
		if (m_instance == null) {
			m_instance = new GlobalDispatcher(true);
		}
		
		return m_instance;
	}
	
	/**
	 * Initializes the singleton instance without the thread timeout.
	 */
	static void junit_instance() {
		/*
		 * We can't hold the lock on the global dispatcher while we wait for
		 * a reset because running objects may need the dispatcher to run.
		 */
		boolean need_reset;
		do {
			need_reset = false;
			synchronized (GlobalDispatcher.class){ 
				if (m_instance != null) {
					need_reset = true;
				}
			}
			
			if (need_reset) {
				reset_instance();
				try {
					Thread.sleep(TERMINATION_POLLING_WAIT_TIME_MS);
				} catch (InterruptedException e) {
					/*
					 * We ignore this.
					 */
				}
			}
		} while (need_reset);
		
		m_instance = new GlobalDispatcher(false);
	}
	
	/**
	 * Resets the singleton stopping any threads. This is generally useful
	 * in unit testing.
	 */
	static void reset_instance() {
		synchronized (GlobalDispatcher.class) {
			if (m_instance != null) {
				try {
					m_instance.m_executor.awaitTermination(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					/*
					 * Ignored.
					 */
				}
				
				m_instance.m_executor.shutdown();
			}
		}
		
		while (true) {
			boolean shutdown = false;
			
			synchronized (GlobalDispatcher.class) {
				if (m_instance == null) {
					break;
				} else {
					shutdown = m_instance.m_executor.isTerminated();
					if (shutdown) {
						m_instance = null;
					}
				}
			}
			
			if (!shutdown) {
				try {
					Thread.sleep(TERMINATION_POLLING_WAIT_TIME_MS);
				} catch (InterruptedException e) {
					/*
					 * Ignored.
					 */
				}
			}
		}
	}
	
	/**
	 * Queues a runnable.
	 * @param r the runnable
	 */
	synchronized void dispatch(Runnable r) {
		Ensure.not_null(r);
		m_executor.execute(new DispatchRunnable(r));
	}
	
	/**
	 * Invoked when an exception is thrown during dispatch.
	 * @param qd the queue dispatch
	 * @param listener the listener that threw the exception
	 * @param t the exception
	 * @param <L> the type of listener
	 */
	<L> void handle_exception(QueuedDispatch<L> qd, L listener, Throwable t) {
		System.err.println("Exception during dispatch: "
				+ t.getClass().getName());
		t.printStackTrace();
	}
	
	/**
	 * Obtains an estimate of the number of tasks executing or queued
	 * for executing.
	 * @return the number of tasks
	 */
	int pending_dispatches() {
		return m_waiting + m_executing;
	}
	
	/**
	 * Runnable that will run another runnable but updating the counts in
	 * the global dispatcher.
	 */
	private class DispatchRunnable implements Runnable {
		/**
		 * The runnable to run.
		 */
		private Runnable m_inner;
		
		/**
		 * Creates a new runnable.
		 * @param inner the inner runnable
		 */
		DispatchRunnable(Runnable inner) {
			Ensure.not_null(inner);
			m_inner = inner;
			
			synchronized (GlobalDispatcher.this) {
				m_waiting++;
			}
		}

		@Override
		public void run() {
			synchronized (GlobalDispatcher.instance()) {
				m_waiting--;
				m_executing++;
			}
			
			try {
				m_inner.run();
			} catch (Throwable t) {
				synchronized (GlobalDispatcher.instance()) {
					if (GlobalDispatcher.instance().m_collector == null) {
						GlobalDispatcher.instance().m_collector =
								new LocalCollector(
								GlobalDispatcher.class.getCanonicalName());
					}
					
					GlobalDispatcher.instance().m_collector.collect(t,
							"dispatch");
				}
			} finally {
				synchronized (GlobalDispatcher.instance()) {
					m_executing--;
				}
			}
		}
		
	}
}
