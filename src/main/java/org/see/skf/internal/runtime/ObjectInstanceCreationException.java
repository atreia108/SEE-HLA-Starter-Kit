package org.see.skf.internal.runtime;

public final class ObjectInstanceCreationException extends Exception {

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
