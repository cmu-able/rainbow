package edu.cmu.cs.able.eseb.filter;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.eseb.BusData;

/**
 * Sink that saves all data received.
 */
public class SaveSink implements EventSink {
	/**
	 * Data sent to the sink.
	 */
	public List<BusData> m_data;
	
	/**
	 * Creates a new sink.
	 */
	public SaveSink() {
		m_data = new ArrayList<>();
	}

	@Override
	public synchronized void sink(BusData data) {
		m_data.add(data);
	}
}
