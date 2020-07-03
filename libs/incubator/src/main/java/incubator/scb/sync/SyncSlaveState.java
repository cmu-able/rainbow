package incubator.scb.sync;

/**
 * State of a synchronization slave.
 */
public enum SyncSlaveState {
	/**
	 * Slave is waiting for synchronization.
	 */
	WAITING,
	
	/**
	 * Slave is synchronizing.
	 */
	SYNCHRONIZING,
	
	/**
	 * Slave is shutting down (or shut down).
	 */
	SHUTDOWN
}
