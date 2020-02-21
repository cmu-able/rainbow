package incubator.scb.sync;

/**
 * Synchronization status of an SCB with respect to the master copy.
 */
public enum SyncStatus {
	/**
	 * We don't know the status of the SCB.
	 */
	UNKNOWN,
	
	/**
	 * The SCB is a master SCB.
	 */
	MASTER,
	
	/**
	 * The SCB is slave SCB synchronized with the master copy.
	 */
	SYNCHRONIZED,
	
	/**
	 * The SCB is a slave SCB that has local changes.
	 */
	LOCAL_CHANGES
}
