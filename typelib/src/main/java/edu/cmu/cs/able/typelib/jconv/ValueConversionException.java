package edu.cmu.cs.able.typelib.jconv;

/**
 * Exception thrown when conversion of a data value fails.
 */
public class ValueConversionException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public ValueConversionException(String description) {
		super(description);
	}
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param cause a cause for the exception
	 */
	public ValueConversionException(String description, Throwable cause) {
		super(description, cause);
	}
}
