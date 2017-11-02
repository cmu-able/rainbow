package edu.cmu.cs.able.eseb.filter;

import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueueGroupImpl;

/**
 * Event sink that sends all events to a bus data que group.
 */
public class BusDataQueueGroupSink implements EventSink {
	/**
	 * The queue group to send events to.
	 */
	private BusDataQueueGroupImpl m_qg;
	
	/**
	 * Creates a new sink.
	 * @param qg the queue group
	 */
	public BusDataQueueGroupSink(BusDataQueueGroupImpl qg) {
		Ensure.not_null(qg);
		m_qg = qg;
	}

	@Override
	public void sink(BusData data) {
		Ensure.not_null(data);
		m_qg.add(data);
	}
}
