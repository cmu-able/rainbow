package edu.cmu.cs.able.typelib.struct;

/**
 * Exception thrown when attempting to define an invalid type.
 */
public class InvalidTypeDefinitionException extends Exception {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception
	 * @param description the description of the exception
	 */
	public InvalidTypeDefinitionException(String description) {
		super(description);
	}
}
