package edu.cmu.cs.able.eseb;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Extension of a data queue which moves all received data from the queue to
 * a list.
 */
public class TestArraySaveQueue extends BusDataQueue {
	/**
	 * The array that receives the values.
	 */
	public List<DataValue> m_values;
	
	/**
	 * Creates a new queue.
	 */
	public TestArraySaveQueue() {
		m_values = new ArrayList<>();
		dispatcher().add(new BusDataQueueListener() {
			@Override
			public void data_added_to_queue() {
				read_from_queue();
			}
		});
	}
	
	/**
	 * Invoked when data has been added to the queue.
	 */
	private synchronized void read_from_queue() {
		BusData bd;
		while ((bd = poll()) != null) {
			m_values.add(bd.value());
		}
	}
}
