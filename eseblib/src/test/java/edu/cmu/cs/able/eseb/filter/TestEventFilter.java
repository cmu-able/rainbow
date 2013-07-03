package edu.cmu.cs.able.eseb.filter;

import java.io.IOException;

import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.BusData;

/**
 * Filter used for tests which may forward or not the received events to the
 * sink.
 */
public class TestEventFilter extends EventFilter {
	/**
	 * Should the received events be forwarded?
	 */
	public boolean m_forward;
	
	/**
	 * Creates a new filter.
	 */
	public TestEventFilter() {
		m_forward = false;
	}

	@Override
	public synchronized void sink(BusData data) throws IOException {
		Ensure.not_null(data);
		if (m_forward) {
			forward(data);
		}
	}
}
