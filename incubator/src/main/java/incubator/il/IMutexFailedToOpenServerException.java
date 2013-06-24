package incubator.il;

/**
 * Exception atirada quando não é possível arrancar o servidor.
 */
public class IMutexFailedToOpenServerException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param cause the exception that caused this one
	 */
	public IMutexFailedToOpenServerException(Exception cause) {
		super("Failed to open info server: " + cause.toString(), cause);
	}
}
