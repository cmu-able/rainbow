package incubator.wt;

import incubator.exh.ThrowableCollector;

/**
 * Control interface for a worker thread. This interface defines what worker
 * thread publicly expose about themselves and can be used to either locally
 * or remotely control a worker thread.
 */
public interface WorkerThreadCI {
	/**
	 * Obtains the name of the thread.
	 * @return the name
	 */
	String name();
	
	/**
	 * Obtains an optional, human-readable description of the thread.
	 * @return a human-readable description of the thread or <code>null</code>
	 * if none
	 */
	String description();
	
	/**
	 * Obtains the state of the thread.
	 * @return the state of the thread
	 */
	WtState state();
	
	/**
	 * Starts the thread.
	 */
	void start();
	
	/**
	 * Stops the thread. Stopping the thread may take a while as this method
	 * will only return after the thread has died.
	 * @throws ClosingWorkerThreadFromWithinException thrown when it is the
	 * working thread that is requesting itself to stop.
	 */
	void stop();
	
	/**
	 * Obtains the collector of throwables with all throwables thrown by this
	 * thread (up to a certain limit). The collector will only have any
	 * information on throwables not caught or handled by the thread.
	 * @return the collector
	 */
	ThrowableCollector collector();
}
