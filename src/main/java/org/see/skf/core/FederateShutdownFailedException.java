package org.see.skf.core;

/**
 *
 * @author Hridyanshu Aatreya
 * @since 2.1
 */
final class FederateShutdownFailedException extends RuntimeException {
    FederateShutdownFailedException(String message) {
        super(message);
    }

    FederateShutdownFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    FederateShutdownFailedException(Throwable cause) {
        super(cause);
    }
}
