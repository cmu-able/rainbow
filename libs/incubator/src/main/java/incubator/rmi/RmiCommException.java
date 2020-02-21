package incubator.rmi;

/**
 * Exception thrown by the RMI communication layer.
 */
public class RmiCommException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param description exception description
	 * @param cause exception cause (may be <code>null</code>)
	 */
	public RmiCommException(String description, Throwable cause) {
		super(description, cause);
	}
}
