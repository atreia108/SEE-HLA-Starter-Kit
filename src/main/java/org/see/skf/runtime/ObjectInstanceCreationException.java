package org.see.skf.runtime;

public final class ObjectInstanceCreationException extends RuntimeException {

    public ObjectInstanceCreationException(String message) {
        super(message);
    }
    public ObjectInstanceCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectInstanceCreationException(Throwable cause) {
        super(cause);
    }
}
