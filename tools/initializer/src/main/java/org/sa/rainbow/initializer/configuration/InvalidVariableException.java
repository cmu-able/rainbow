package org.sa.rainbow.initializer.configuration;

import java.io.IOException;

public class InvalidVariableException  extends IOException {
    public InvalidVariableException() {
    }

    public InvalidVariableException(String message) {
        super(message);
    }

    public InvalidVariableException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidVariableException(Throwable cause) {
        super(cause);
    }
}