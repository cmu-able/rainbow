/**
 * Created November 22, 2006.
 */
package org.sa.rainbow.core.error;

/**
 * This RuntimeException is thrown anytime a Probe Lifecycle transition is done
 * that is not valid, i.e., doesn't follow one of the allowed transitions.
 * @see <code>IProbe</code>
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class BadLifecycleStepException extends RuntimeException {
	private static final long serialVersionUID = -6100659947752279326L;

	/**
	 * Default Constructor, no message.
	 */
	public BadLifecycleStepException () {
		this("A bad Probe Lifecycle transition just occurred!");
	}

	public BadLifecycleStepException (String message) {
		super(message);
	}

	public BadLifecycleStepException (Throwable cause) {
		super(cause);
	}

	public BadLifecycleStepException (String message, Throwable cause) {
		super(message, cause);
	}

}
