package org.see.skf.runtime;

public final class AnnotationParseException extends RuntimeException {

    public AnnotationParseException(String message) {
        super(message);
    }

    public AnnotationParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationParseException(Throwable cause) {
        super(cause);
    }
}
