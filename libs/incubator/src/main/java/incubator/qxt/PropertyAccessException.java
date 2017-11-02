package incubator.qxt;

/**
 * Exception thrown when trying to access (read or write) a bean property.
 * 
 * @see QxtRealProperty#getValue(Object)
 * @see QxtRealProperty#setValue(Object, Object)
 */
public class PropertyAccessException extends RuntimeException {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * 
	 * @param description a description of the exception
	 */
	public PropertyAccessException(String description) {
		super(description);
	}

	/**
	 * Creates a new exception.
	 * 
	 * @param description a description of the exception
	 * @param cause the cause of this exception (may be <code>null</code>)
	 */
	public PropertyAccessException(String description, Throwable cause) {
		super(description, cause);
	}
}
