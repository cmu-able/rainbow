package incubator.dispatch;

import auxtestlib.AbstractTestHelper;

/**
 * Helper class that will ensure the global dispatch is reset between uses
 * and all used threads are destroyed when the dispatch is finished.
 */
public class DispatchHelper extends AbstractTestHelper {
	/**
	 * Interval, in milliseconds, of the time to retry to see if the dispatcher
	 * has changed.
	 */
	private static final long DISPATCH_RETRY_MS = 5;
	
	/**
	 * Creates a new helper.
	 * @throws Exception creation failed
	 */
	public DispatchHelper() throws Exception {
		super();
	}

	@Override
	protected void mySetUp() throws Exception {
		GlobalDispatcher.junit_instance();
	}

	@Override
	protected void myTearDown() throws Exception {
		GlobalDispatcher.reset_instance();
	}

	@Override
	protected void myCleanUp() throws Exception {
		GlobalDispatcher.reset_instance();
	}

	@Override
	protected void myPrepareFixture() throws Exception {
		GlobalDispatcher.junit_instance();
	}
	
	/**
	 * Sleeps the current thread until the global dispatcher has cleared
	 * all events.
	 * @throws Exception failed
	 */
	public void wait_dispatch_clear() throws Exception {
		while (GlobalDispatcher.instance().pending_dispatches() != 0) {
			Thread.sleep(DISPATCH_RETRY_MS);
		}
	}
}
