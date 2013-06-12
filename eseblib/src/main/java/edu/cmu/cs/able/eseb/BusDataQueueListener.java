package edu.cmu.cs.able.eseb;

/**
 * Listener that is informed when data is added to an bus data queue.
 */
public interface BusDataQueueListener {
	/**
	 * Data has been added to the queue.
	 */
	void data_added_to_queue();
}
