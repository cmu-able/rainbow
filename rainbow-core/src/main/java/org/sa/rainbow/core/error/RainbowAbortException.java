/**
 * Created May 8, 2007.
 */
package org.sa.rainbow.core.error;


/**
 * This RuntimeException may be thrown when Rainbow cannot continue to run,
 * in particular during initialization when certain required resources are
 * not found.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class RainbowAbortException extends RuntimeException {
	private static final long serialVersionUID = 5161763615812106395L;

	/**
	 * Default Constructor, no message.
	 */
	public RainbowAbortException () {
		this("Rainbow doesn't know how to proceed, aborted!");
	}

	public RainbowAbortException (String message) {
		super(message);
	}

	public RainbowAbortException (Throwable cause) {
		super(cause);
	}

	public RainbowAbortException (String message, Throwable cause) {
		super(message, cause);
	}

}
