package edu.cmu.cs.able.eseb.rpc;

/**
 * Exception thrown when an operation execution reports a failure.
 */
public class OperationFailureException extends OperationException {
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
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param cause the cause of the exception
	 */
	public OperationFailureException(String description, Throwable cause) {
		super(description, cause);
	}
}
