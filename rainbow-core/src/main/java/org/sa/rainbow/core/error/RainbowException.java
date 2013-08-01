package org.sa.rainbow.core.error;


public class RainbowException extends Exception {

    public RainbowException () {
        super ();
    }

    public RainbowException (String message) {
        super (message);
    }

    public RainbowException (Throwable e) {
        super (e);
    }

    public RainbowException (String message, Throwable cause) {
        super (message, cause);
    }

}
