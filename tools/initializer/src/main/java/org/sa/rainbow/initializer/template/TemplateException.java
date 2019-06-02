package org.sa.rainbow.initializer.template;

import java.io.IOException;

/**
 * Signals that a template or a set of templates cannot be read or parsed.
 *
 * @author Jiahui Feng
 * @since 1.0
 */
public class TemplateException extends IOException {
    public TemplateException() {
    }

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateException(Throwable cause) {
        super(cause);
    }
}
