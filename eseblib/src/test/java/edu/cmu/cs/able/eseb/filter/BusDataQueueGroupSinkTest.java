package edu.cmu.cs.able.eseb.filter;

import incubator.dispatch.DispatchHelper;

import org.junit.Test;

import auxtestlib.TestHelper;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueGroupImpl;

/**
 * Tests sinks to bus data queues.
 */
@SuppressWarnings("javadoc")
public class BusDataQueueGroupSinkTest extends FilterTestCase {
	@TestHelper
	private DispatchHelper m_dh;
	
	@Test
	public void sink() throws Exception {
		BusDataQueueGroupImpl qg = new BusDataQueueGroupImpl();
		BusDataQueue q = new BusDataQueue();
		qg.add(q);
		BusDataQueueGroupSink s = new BusDataQueueGroupSink(qg);
		
		s.sink(bus_data());
		m_dh.wait_dispatch_clear();
		
		assertNotNull(q.poll());
		assertNull(q.poll());
	}
	
	@Test(expected = AssertionError.class)
	public void cannot_sink_null() throws Exception {
		BusDataQueueGroupImpl qg = new BusDataQueueGroupImpl();
		BusDataQueue q = new BusDataQueue();
		qg.add(q);
		BusDataQueueGroupSink s = new BusDataQueueGroupSink(qg);
		s.sink(null);
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void cannot_create_with_null_queue_group() throws Exception {
		new BusDataQueueGroupSink(null);
	}
}
