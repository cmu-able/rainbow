package incubator.exh;

import auxtestlib.AbstractTestHelper;

/**
 * Test helper that turns collected stack traces on.
 */
public class ExhHelper extends AbstractTestHelper {
	/**
	 * Creates new test helper.
	 * @throws Exception failed to create the test helper.
	 */
	public ExhHelper() throws Exception {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected void mySetUp() throws Exception {
		ThrowableCollector.print_stack_trace(true);
	}

	@Override
	protected void myTearDown() throws Exception {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected void myCleanUp() throws Exception {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected void myPrepareFixture() throws Exception {
		/*
		 * Nothing to do.
		 */
	}
}
