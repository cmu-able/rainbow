package edu.cmu.cs.able.eseb;

import incubator.dispatch.DispatchHelper;
import incubator.exh.ExhHelper;
import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Default test case for eseb tests.
 */
public class EsebTestCase extends DefaultTCase {
	/**
	 * Helper for dispatcher.
	 */
	@TestHelper
	public DispatchHelper m_dispatch_helper;
	
	/**
	 * Helper that ensures all threads are shut down.
	 */
	@TestHelper
	public ThreadCountTestHelper m_thread_count_helper;
	
	/**
	 * Helper that ensure the throwable collector is property set up.
	 */
	@TestHelper
	public ExhHelper m_exh_helper;
}
