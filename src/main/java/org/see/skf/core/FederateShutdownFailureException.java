package org.see.skf.core;

/**
 *
 * @author Hridyanshu Aatreya
 * @since 2.1
 */
public class FederateShutdownFailureException extends RuntimeException {
    public FederateShutdownFailureException(String message) {
        super(message);
    }

    public FederateShutdownFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public FederateShutdownFailureException(Throwable cause) {
        super(cause);
    }
}
