package auxtestlib;

/**
 * Exception thrown when a test case configuration (or setup) is incorrect.
 */
public class TestCaseConfigurationException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;

	/**
	 * Creates a new exception.
	 * 
	 * @param description description of the exception
	 */
	public TestCaseConfigurationException(String description) {
		super(description);
	}

	/**
	 * Creates a new exception.
	 * 
	 * @param description description of the exception
	 * @param cause cause of the exception
	 */
	public TestCaseConfigurationException(String description, Throwable cause) {
		super(description, cause);
	}
}
