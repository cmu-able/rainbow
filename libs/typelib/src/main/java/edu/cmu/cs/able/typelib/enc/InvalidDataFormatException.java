package edu.cmu.cs.able.typelib.enc;

/**
 * This exception is thrown when data being read from the string does not
 * match the expected format or contains invalid data.
 */
@Deprecated
public class InvalidDataFormatException extends Exception {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new exception.
	 * @param description the exception message
	 */
	public InvalidDataFormatException(String description) {
		super(description);
	}
}