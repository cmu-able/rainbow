package incubator.dispatch;

import auxtestlib.AbstractTestHelper;

/**
 * Helper class that will ensure the global dispatch is reset between uses
 * and all used threads are destroyed when the dispatch is finished.
 */
public class DispatchHelper extends AbstractTestHelper {
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
}
