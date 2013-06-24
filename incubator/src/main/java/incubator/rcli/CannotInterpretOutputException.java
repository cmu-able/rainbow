package incubator.rcli;

/**
 * Exception thrown when the output of a command cannot be interpreted.
 */
public class CannotInterpretOutputException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public CannotInterpretOutputException(String description) {
		super(description);
	}
}
