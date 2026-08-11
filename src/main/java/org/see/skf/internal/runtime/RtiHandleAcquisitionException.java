package org.see.skf.internal.runtime;

public final class RtiHandleAcquisitionException extends RuntimeException {
    public RtiHandleAcquisitionException(String message) {
        super(message);
    }

    public RtiHandleAcquisitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RtiHandleAcquisitionException(Throwable cause) {
        super(cause);
    }
}
