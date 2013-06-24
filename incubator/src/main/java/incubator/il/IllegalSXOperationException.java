package incubator.il;

/**
 * Exception thrown when trying to promote or demote a lock that doesn't
 * exist.
 */
public class IllegalSXOperationException extends RuntimeException {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception
	 * @param description a description of the exception
	 */
	public IllegalSXOperationException(String description) {
		super(description);
	}
}
