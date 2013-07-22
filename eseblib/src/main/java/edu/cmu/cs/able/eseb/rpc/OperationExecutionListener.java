package edu.cmu.cs.able.eseb.rpc;

/**
 * Listener that can be added to a {@link OperationExecution} to be informed
 * when operation execution is complete.
 */
public interface OperationExecutionListener {
	/**
	 * Invoked when the operation is complete.
	 */
	void operation_complete();
}
