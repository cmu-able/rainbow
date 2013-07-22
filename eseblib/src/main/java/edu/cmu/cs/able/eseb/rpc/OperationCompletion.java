package edu.cmu.cs.able.eseb.rpc;

/**
 * Types of operation completion.
 */
public enum OperationCompletion {
	/**
	 * Operation completed successfully.
	 */
	SUCCESSFUL,
	
	/**
	 * Operation execution timed out.
	 */
	TIMED_OUT,
	
	/**
	 * Operation execution has failed.
	 */
	FAILED
}
