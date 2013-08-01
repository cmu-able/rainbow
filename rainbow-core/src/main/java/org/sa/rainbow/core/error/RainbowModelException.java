package org.sa.rainbow.core.error;

public class RainbowModelException extends RainbowException {

    public RainbowModelException (String message) {
        super (message);
    }

    public RainbowModelException (Throwable cause) {
        super (cause);
    }

    public RainbowModelException (String message, Exception e) {
        super (message, e);
    }

}
