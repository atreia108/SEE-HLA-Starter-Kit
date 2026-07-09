package org.see.skf.core;

public final class FederateShutdownInterruptedException extends Exception {
    FederateShutdownInterruptedException(String message) {
        super(message);
    }

    FederateShutdownInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    FederateShutdownInterruptedException(Throwable cause) {
        super(cause);
    }
}
