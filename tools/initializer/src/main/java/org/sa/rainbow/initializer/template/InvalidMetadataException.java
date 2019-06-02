package org.sa.rainbow.initializer.template;

/**
 * Signals that metadata cannot be parsed.
 *
 * @author Jiahui Feng
 * @since 1.0
 */

public class InvalidMetadataException extends TemplateException {
    public InvalidMetadataException() {
    }

    public InvalidMetadataException(String message) {
        super(message);
    }

    public InvalidMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMetadataException(Throwable cause) {
        super(cause);
    }
}
