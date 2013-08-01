package org.sa.rainbow.core.error;

import java.io.IOException;

public class RainbowConnectionException extends RainbowException {

    public RainbowConnectionException (String string) {
        super (string);
    }

    public RainbowConnectionException (String message, IOException e) {
        super (message, e);
    }

}
