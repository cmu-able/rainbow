package edu.cmu.cs.able.eseb;

/**
 * Remote interface that provides access to the bus server.
 */
public interface BusServerRemoteInterface {
	/**
	 * Obtains the port where the server is running.
	 * @return the port
	 */
	short port();
	
	/**
	 * Obtains the distribution queue with a given key. The key is generated
	 * by the invoker. If this method has never been invoked with the given
	 * key, it will return an empty queue. From that invocation onwards, all
	 * invocations of this method with the same key will return queues with
	 * the new events since the last invocation. If this method is never
	 * invoked after a long time, the server may drop the key to save memory
	 * and the next invocation will contain act as a first one.
	 * @param key the client-generated unique key
	 * @return the queue update
	 */
	LimitedDistributionQueue distribution_queue(String key);
}
