/**
 * Created April 11, 2007.
 */
package org.sa.rainbow.core.error;

import org.sa.rainbow.core.RainbowConstants;

/**
 * This RuntimeException may be thrown when tracking error count in some Rainbow
 * context, and the error count reaches a maximum threshold, e.g.,
 * {@linkplain RainbowConstants.MAX_ERROR_CNT}.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ErrorsReachedThresholdException extends RuntimeException {
    private static final long serialVersionUID = -7977873354537853809L;

    /**
     * Default Constructor, no message.
     */
    public ErrorsReachedThresholdException () {
        this("Error count just reached a maximum threshold!");
    }

    public ErrorsReachedThresholdException (String message) {
        super(message);
    }

    public ErrorsReachedThresholdException (Throwable cause) {
        super(cause);
    }

    public ErrorsReachedThresholdException (String message, Throwable cause) {
        super(message, cause);
    }

}
