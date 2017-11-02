package incubator.wt;

/**
 * Exception thrown when closing the worker thread from within itself. If
 * this exception propagates to the main loop of the worker thread, it will
 * exit gracefully.
 */
public class ClosingWorkerThreadFromWithinException extends RuntimeException {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new exception.
	 */
	public ClosingWorkerThreadFromWithinException() {
	}
}
