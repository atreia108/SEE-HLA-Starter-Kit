package org.see.skf.core;

public class FederateShutdownAbortedException extends Exception{
    public FederateShutdownAbortedException(String message) {
        super(message);
    }

    public FederateShutdownAbortedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FederateShutdownAbortedException(Throwable cause) {
        super(cause);
    }
}
