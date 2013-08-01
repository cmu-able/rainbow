package edu.cmu.cs.able.eseb.rpc;

/**
 * Contains information about a failure which resulted from an operation
 * execution. Every failure has three pieces of information: the
 * <em>type</em> of failure, <em>e.g.</em> the type of exception, the
 * <em>description</em> of the failure, a user-readable message about the
 * failure, <em>e.g.</em> the exception message, and failure <em>data</em>,
 * additional information about the failure, <em>e.g.</em> the exception
 * stack trace.
 */
public class FailureInformation {
	/**
	 * Creates a new failure information.
	 * @param type the type of failure
	 * @param description the description of the failure
	 * @param data the failure data
	 */
	public FailureInformation(String type, String description, String data) {
	}
	
	/**
	 * Obtains the type of failure.
	 * @return the type of failure
	 */
	public String type() {
		return null;
	}
	
	/**
	 * Obtains the description of the failure.
	 * @return the description of the failure
	 */
	public String description() {
		return null;
	}
	
	/**
	 * Obtains the failure data.
	 * @return the failure data
	 */
	public String data() {
		return null;
	}
}
