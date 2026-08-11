package org.see.skf.internal.runtime;

final class AnnotationParseException extends RuntimeException {

    AnnotationParseException(String message) {
        super(message);
    }

    AnnotationParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
