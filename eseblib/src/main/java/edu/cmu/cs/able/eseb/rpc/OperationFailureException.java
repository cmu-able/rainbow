package edu.cmu.cs.able.eseb.rpc;

/**
 * Exception thrown when an operation execution reports a failure.
 */
public class OperationFailureException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public OperationFailureException(String description) {
		super(description);
	}
}
