package edu.cmu.cs.able.typelib.scope;

/**
 * Exception thrown when scopes are linked in a cyclic structure.
 */
public class CyclicScopeLinkageException extends Exception {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public CyclicScopeLinkageException(String description) {
		super(description);
	}
}
