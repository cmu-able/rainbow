package incubator.qxt;

/**
 * Exception thrown when failure to access the definition of a property.
 */
public class PropertyDefinitionException extends RuntimeException {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * 
	 * @param description a description of the exception
	 */
	public PropertyDefinitionException(String description) {
		super(description);
	}

	/**
	 * Creates a new exception.
	 * 
	 * @param description a description of the exception
	 * @param cause the cause of this exception (may be <code>null</code>)
	 */
	public PropertyDefinitionException(String description, Throwable cause) {
		super(description, cause);
	}
}
