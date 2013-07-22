package edu.cmu.cs.able.eseb.rpc;

/**
 * Exception thrown when synchronous execution of an operation has timed out.
 */
public class OperationTimedOutException extends OperationException {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param description an execution of the exception
	 */
	public OperationTimedOutException(String description) {
		super(description);
	}
}
