package edu.cmu.cs.able.typelib.struct;

/**
 * Exception thrown when accessing a field of a structure that does not
 * exist.
 */
public class UnknownFieldException extends RuntimeException {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public UnknownFieldException(String description) {
		super(description);
	}
}
