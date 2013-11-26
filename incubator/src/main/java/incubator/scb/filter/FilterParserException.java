package incubator.scb.filter;

/**
 * Exception thrown when failed to parser a filter.
 */
public class FilterParserException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param cause a cause for the exception
	 */
	public FilterParserException(Throwable cause) {
		super(cause);
	}

}
