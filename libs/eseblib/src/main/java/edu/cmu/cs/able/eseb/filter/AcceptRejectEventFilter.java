package edu.cmu.cs.able.eseb.filter;

import java.io.IOException;

import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.BusData;

/**
 * Filter which lets events pass through or discards events.
 */
public abstract class AcceptRejectEventFilter extends EventFilter {
	/**
	 * Creates a new filter.
	 */
	public AcceptRejectEventFilter() {
	}
	
	@Override
	public final void sink(BusData data) throws IOException {
		Ensure.not_null(data);
		if (accepts(data)) {
			forward(data);
		}
	}
	
	/**
	 * Checks whether the filter accepts or rejects the a given event.
	 * @param d the event to check
	 * @return should the event data be sent to the sink?
	 */
	protected abstract boolean accepts(BusData d);
}
