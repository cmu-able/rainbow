package edu.cmu.cs.able.eseb;

/**
 * Group of queues that receive information. This interface is used to
 * register queues in a group so that all of them are notified when events
 * are received from the bus.
 */
public interface BusDataQueueGroup {
	/**
	 * Adds a new queue to the group.
	 * @param q the queue
	 */
	void add(BusDataQueue q);
}
