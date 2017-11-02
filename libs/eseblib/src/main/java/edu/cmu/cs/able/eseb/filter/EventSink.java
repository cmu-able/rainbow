package edu.cmu.cs.able.eseb.filter;

import java.io.IOException;

import edu.cmu.cs.able.eseb.BusData;

/**
 * An event sink is an object that receives events. This interface does not
 * specify what the sink does with the events.
 */
public interface EventSink {
	/**
	 * Sinks the given event.
	 * @param data the event
	 * @throws IOException failed to send the data
	 */
	void sink(BusData data) throws IOException;
}
