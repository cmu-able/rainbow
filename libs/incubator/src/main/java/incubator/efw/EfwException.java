package incubator.efw;

/**
 * Global exception of the <code>efw</code> package.
 */
public class EfwException extends Exception {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * 
	 * @param description the exception description
	 */
	public EfwException(String description) {
		super(description);
	}
	
	/**
	 * Creates a new exception.
	 * 
	 * @param description the exception description
	 * @param cause the exception that caused this one to fire
	 */
	public EfwException(String description, Throwable cause) {
		super(description, cause);
	}
}
