package incubator.rmi;

import incubator.rmi.RmiServerPublisher;
import auxtestlib.AbstractTestHelper;

/**
 * Helper for unit tests that ensures that, at the end of the test, all
 * RMI services are closed and the ports freed.
 */
public class RmiHelper extends AbstractTestHelper {
	/**
	 * Creates a new helper.
	 * @throws Exception set up failed
	 */
	public RmiHelper() throws Exception {
		super();
	}

	@Override
	protected void mySetUp() throws Exception {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected void myTearDown() throws Exception {
		RmiServerPublisher.shutdown_all();
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
