package edu.cmu.cs.able.typelib.scope;

/**
 * Exception thrown when searching for an object that is found in multiple
 * linked scopes.
 */
public class AmbiguousNameException extends Exception {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param description description
	 */
	public AmbiguousNameException(String description) {
		super(description);
	}
}
