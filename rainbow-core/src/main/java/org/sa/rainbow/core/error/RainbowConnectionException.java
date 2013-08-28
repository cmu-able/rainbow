package org.sa.rainbow.core.error;


public class RainbowConnectionException extends RainbowException {

    public RainbowConnectionException (String string) {
        super (string);
    }

    public RainbowConnectionException (String message, Exception e) {
        super (message, e);
    }

}
