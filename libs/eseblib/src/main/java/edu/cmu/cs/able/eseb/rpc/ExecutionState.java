package edu.cmu.cs.able.eseb.rpc;

/**
 * State of an operation execution.
 */
public enum ExecutionState {
	/**
	 * Executed has not started.
	 */
	NOT_STARTED,
	
	/**
	 * Execution is pending.
	 */
	PENDING,
	
	/**
	 * Execution is complete.
	 */
	COMPLETED
}
