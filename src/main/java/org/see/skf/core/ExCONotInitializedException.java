package org.see.skf.core;

public class ExCONotInitializedException extends RuntimeException {
    public ExCONotInitializedException(String message) {
        super(message);
    }

    public ExCONotInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExCONotInitializedException(Throwable cause) {
        super(cause);
    }
}
