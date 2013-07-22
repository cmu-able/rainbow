package edu.cmu.cs.able.eseb.filter.participant;

/**
 * Exception thrown when there is a problem related to participants.
 */
public class ParticipantException extends Exception {
	/**
	 * Version for serialization.
	 */
	public static final long serialVersionUID = 1;
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 */
	public ParticipantException(String description) {
		super(description);
	}
	
	/**
	 * Creates a new exception.
	 * @param description a description of the exception
	 * @param cause the cause of the exception (may be <code>null</code>)
	 */
	public ParticipantException(String description, Throwable cause) {
		super(description, cause);
	}
}
