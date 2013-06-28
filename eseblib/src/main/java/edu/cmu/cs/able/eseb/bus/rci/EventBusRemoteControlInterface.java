package edu.cmu.cs.able.eseb.bus.rci;


/**
 * Remote control interface for the event bus. This interface is not used to
 * publish or subscribe to the event bus. It is used to query the bus for
 * administrative and control data.
 */
public interface EventBusRemoteControlInterface {
	/**
	 * Obtains the port where the bus is running.
	 * @return the port
	 */
	short port();
	
	/**
	 * Obtains the port where the data master is running.
	 * @return the data master port
	 */
	short data_master_port();
	
	/**
	 * Obtains the distribution queue with a given key. The key is generated
	 * by the invoker. If this method has never been invoked with the given
	 * key, it will return an empty queue. From that invocation onwards, all
	 * invocations of this method with the same key will return queues with
	 * the new events since the last invocation. If this method is never
	 * invoked after a long time, the bus may drop the key to save memory
	 * and the next invocation will contain act as a first one.
	 * @param key the invoker-generated unique key
	 * @return the queue update
	 */
	LimitedDistributionQueue distribution_queue(String key);
}
