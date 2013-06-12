package edu.cmu.cs.able.parsec;

/**
 * Exception thrown when failing to parse a block text.
 */
public class BlockTextParseException extends Exception {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param e the exception containing the parse error with coordinates
	 * relative to the block text start
	 */
	public BlockTextParseException(LocalizedParseException e) {
		super(e);
	}
}
