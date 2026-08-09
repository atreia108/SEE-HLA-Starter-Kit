package org.see.skf.internal.runtime;

public final class RtiHandleException extends RuntimeException {
    public RtiHandleException(String message) {
        super(message);
    }

    public RtiHandleException(String message, Throwable cause) {
        super(message, cause);
    }

    public RtiHandleException(Throwable cause) {
        super(cause);
    }
}
