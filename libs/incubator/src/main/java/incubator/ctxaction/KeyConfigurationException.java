package incubator.ctxaction;

/**
 * Exception thrown when an invalid fixed key configuration is found.
 */
public class KeyConfigurationException extends RuntimeException {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * 
	 * @param description a description of the exception
	 */
	public KeyConfigurationException(String description) {
		super(description);
	}
}
