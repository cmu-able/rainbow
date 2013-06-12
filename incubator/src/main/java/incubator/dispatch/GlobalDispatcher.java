package incubator.dispatch;

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
	private static final int INACTIVITY_TIMEOUT_MS = 250;
	
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
	static synchronized void junit_instance() {
		if (m_instance != null) {
			reset_instance();
		}
		
		m_instance = new GlobalDispatcher(false);
	}
	
	/**
	 * Resets the singleton stopping any threads. This is generally useful
	 * in unit testing.
	 */
	static void reset_instance() {
		synchronized (GlobalDispatcher.class) {
			if (m_instance != null) {
				m_instance.m_executor.shutdownNow();
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
	void dispatch(Runnable r) {
		Ensure.notNull(r);
		m_executor.execute(r);
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
}
