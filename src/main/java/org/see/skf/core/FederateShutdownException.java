package org.see.skf.core;

public final class FederateShutdownException extends RuntimeException {
    FederateShutdownException(String message) {
        super(message);
    }

    FederateShutdownException(String message, Throwable cause) {
        super(message, cause);
    }

    FederateShutdownException(Throwable cause) {
        super(cause);
    }
}
