package org.sa.rainbow.initializer.configuration;

import java.io.IOException;

public class InvalidVariableException extends IOException {
    public InvalidVariableException(String message) {
        super(message);
    }
}