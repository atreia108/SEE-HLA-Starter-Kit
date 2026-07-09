package org.see.skf.runtime;

public final class RtiHandleRetrievalException extends RuntimeException {
    public RtiHandleRetrievalException(String message) {
        super(message);
    }

    public RtiHandleRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }

    public RtiHandleRetrievalException(Throwable cause) {
        super(cause);
    }
}
