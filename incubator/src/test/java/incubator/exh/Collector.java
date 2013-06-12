package incubator.exh;

import incubator.dispatch.DispatchHelper;

import java.util.Date;

import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.ThreadCountTestHelper;

/**
 * Tests the throwable collector.
 */
public class Collector extends DefaultTCase {
	/**
	 * Helper that resets the global dispatcher.
	 */
	@TestHelper
	private DispatchHelper m_dispatch_helper;
	
	/**
	 * Helper that ensures we close all threads.
	 */
	@TestHelper
	private ThreadCountTestHelper m_thread_count_helper;
	
	/**
	 * Ensures that the collector can collect throwables and throws away old
	 * throwables.
	 * @throws Exception test failed
	 */
	@Test
	public void collects_throwables() throws Exception {
		ThrowableCollector collector = new ThrowableCollector("foo");
		collector.max_size(2);
		
		Exception t1 = new Exception("foo");
		RuntimeException t2 = new RuntimeException("bar");
		Error t3 = new Error("glu");
		
		assertEquals(0, collector.throwables().size());
		
		Date mid_1_date = new Date(); 
		
		collector.collect(t1, "xx");
		assertEquals(1, collector.throwables().size());
		assertEquals(t1, collector.throwables().get(0).throwable());
		assertEquals("xx", collector.throwables().get(0).location());
		assertTrue(!collector.throwables().get(0).when().before(mid_1_date));
		assertTrue(!collector.throwables().get(0).when().after(new Date()));
		
		Date mid_2_date = new Date(); 
		
		collector.collect(t2, "yy");
		assertEquals(2, collector.throwables().size());
		assertEquals(t1, collector.throwables().get(0).throwable());
		assertEquals(t2, collector.throwables().get(1).throwable());
		assertEquals("xx", collector.throwables().get(0).location());
		assertEquals("yy", collector.throwables().get(1).location());
		assertTrue(!collector.throwables().get(0).when().before(mid_1_date));
		assertTrue(!collector.throwables().get(0).when().after(mid_2_date));
		assertTrue(!collector.throwables().get(1).when().before(mid_2_date));
		assertTrue(!collector.throwables().get(1).when().after(new Date()));
		
		Date mid_3_date = new Date(); 
		
		collector.collect(t3, "zz");
		assertEquals(2, collector.throwables().size());
		assertEquals(t2, collector.throwables().get(0).throwable());
		assertEquals(t3, collector.throwables().get(1).throwable());
		assertEquals("yy", collector.throwables().get(0).location());
		assertEquals("zz", collector.throwables().get(1).location());
		assertTrue(!collector.throwables().get(0).when().before(mid_2_date));
		assertTrue(!collector.throwables().get(0).when().after(mid_3_date));
		assertTrue(!collector.throwables().get(1).when().before(mid_3_date));
		assertTrue(!collector.throwables().get(1).when().after(new Date()));
	}
}
