/**
 * Created March 8, 2007.
 */
package org.sa.rainbow.stitch.error;

/**
 * This exception is thrown when either the Tactic or Strategy has been invoked
 * with arguments that are mismatched from the formal parameters. 
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ArgumentMismatchException extends RuntimeException {

	private static final long serialVersionUID = 3556065601989614311L;

	/**
	 * Default constructor.
	 */
	public ArgumentMismatchException() {
		super("");
	}

	/**
	 * @param message
	 */
	public ArgumentMismatchException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ArgumentMismatchException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ArgumentMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

}
