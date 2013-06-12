package edu.cmu.cs.able.typelib.enc;

/**
 * Exception thrown when decoding data from base types fails.
 */
public class InvalidEncodingException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public InvalidEncodingException(String description) {
		super(description);
	}

	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param cause a cause for this exception
	 */
	public InvalidEncodingException(String description, Throwable cause) {
		super(description, cause);
	}
}
