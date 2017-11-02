package incubator.bpref;

/**
 * Exception thrown when configuring a bean prefs synchronizer.
 */
public class BeanPrefsSynchronizerException extends Exception {
	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * 
	 * @param description a description of the exception
	 */
	public BeanPrefsSynchronizerException(String description) {
		super(description);
	}

	/**
	 * Creates a new exception.
	 * 
	 * @param description a description of the exception
	 * @param cause the cause of the exception
	 */
	public BeanPrefsSynchronizerException(String description, Throwable cause) {
		super(description, cause);
	}
}
