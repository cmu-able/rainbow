package edu.cmu.cs.able.eseb.rpc;

/**
 * Exception thrown when processing a Java RPC service whose definition is
 * incorrect.
 */
public class IllegalServiceDefinitionException extends RuntimeException {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates the new exception.
	 * @param description a description of the failure
	 */
	public IllegalServiceDefinitionException(String description) {
		super(description);
	}
	
	/**
	 * Creates the new exception.
	 * @param description a description of the failure
	 * @param cause a cause of the exception
	 */
	public IllegalServiceDefinitionException(String description,
			Throwable cause) {
		super(description, cause);
	}
}
