package auxtestlib;

/**
 * Exception thrown when a command failed to execute.
 */
public class CommandExecutionException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;

	/**
	 * Creates a new exception.
	 * 
	 * @param description description of the exception
	 */
	public CommandExecutionException(String description) {
		super(description);
	}

	/**
	 * Creates a new exception.
	 * 
	 * @param description description of the exception
	 * @param cause cause of the exception
	 */
	public CommandExecutionException(String description, Throwable cause) {
		super(description, cause);
	}
}
