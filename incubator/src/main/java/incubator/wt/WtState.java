package incubator.wt;

/**
 * State of a worker thread.
 */
public enum WtState {
	/**
	 * Thread is stopped.
	 */
	STOPPED,
	
	/**
	 * Thread is running.
	 */
	RUNNING,
	
	/**
	 * Thread is started.
	 */
	STARTING,
	
	/**
	 * Thread is shutting down.
	 */
	STOPPING,
	
	/**
	 * Thread has been aborted by an exception.
	 */
	ABORTED
	
}