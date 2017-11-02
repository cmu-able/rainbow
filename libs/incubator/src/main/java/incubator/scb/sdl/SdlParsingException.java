package incubator.scb.sdl;

/**
 * Exception thrown when failed to parse an SDL definition.
 */
public class SdlParsingException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public SdlParsingException(String description) {
		super(description);
	}
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param cause a cause for the exception
	 */
	public SdlParsingException(String description, Throwable cause) {
		super(description, cause);
	}
}
