package incubator.wt;

import java.io.Closeable;

/**
 * Worker thread used for testing purposes.
 * @param <T> the closeable type
 */
public class TestCloseableWorkerThread<T extends Closeable>
		extends CloseableWorkerThread<T>{
	/**
	 * Exception to throw in cycle, if any.
	 */
	public Exception m_to_throw;
	
	/**
	 * Creates a new thread.
	 * @param t the closeable
	 * @param close_on_abort should the closeable be closed on abort?
	 */
	public TestCloseableWorkerThread(T t, boolean close_on_abort) {
		super("x", t, close_on_abort);
	}

	@Override
	protected synchronized void do_cycle_operation(T closeable)
			throws Exception {
		if (m_to_throw != null) {
			throw m_to_throw;
		}
		
		wait(10);
	}
}
