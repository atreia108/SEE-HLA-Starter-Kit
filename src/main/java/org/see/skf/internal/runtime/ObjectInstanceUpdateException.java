package org.see.skf.internal.runtime;

public final class ObjectInstanceUpdateException extends RuntimeException {

    public ObjectInstanceUpdateException(String message) {
        super(message);
    }

    public ObjectInstanceUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectInstanceUpdateException(Throwable cause) {
        super(cause);
    }

}
