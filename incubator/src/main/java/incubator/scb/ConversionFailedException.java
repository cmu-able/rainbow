package incubator.scb;

/**
 * Exception thrown when value conversion fails.
 */
public class ConversionFailedException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public ConversionFailedException(String description) {
		super(description);
	}
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param t an optional cause for this exception
	 */
	public ConversionFailedException(String description, Throwable t) {
		super(description, t);
	}
}
