package incubator.scb.sync;

/**
 * Exception thrown when a reference to a container with an unknown key is
 * made.
 */
public class UnknownContainerException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new exception.
	 * @param key the unknown container key
	 */
	public UnknownContainerException(String key) {
		super("Unknown container with key '" + key + "'.");
	}
}
