package incubator.scb.sdl;

/**
 * Exception thrown when failed to generate SDL code.
 */
public class SdlGenerationException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public SdlGenerationException(String description) {
		super(description);
	}
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param cause a cause of the exception
	 */
	public SdlGenerationException(String description, Throwable cause) {
		super(description, cause);
	}
}
