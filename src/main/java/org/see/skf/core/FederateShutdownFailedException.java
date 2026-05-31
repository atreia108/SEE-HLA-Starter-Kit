package org.see.skf.core;

/**
 *
 * @author Hridyanshu Aatreya
 * @since 2.1
 */
public class FederateShutdownFailedException extends RuntimeException {
    public FederateShutdownFailedException(String message) {
        super(message);
    }

    public FederateShutdownFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FederateShutdownFailedException(Throwable cause) {
        super(cause);
    }
}
