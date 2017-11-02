package edu.cmu.cs.able.eseb.rpc;

/**
 * Exception thrown by the operation package.
 */
public class OperationException extends RuntimeException {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public OperationException(String description) {
		super(description);
	}
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param cause a possible cause of this exception
	 */
	public OperationException(String description, Throwable cause) {
		super(description, cause);
	}
}
