package org.see.skf.core;

public final class ExCONotInitializedException extends RuntimeException {

    public ExCONotInitializedException(String message) {
        super(message);
    }

    public ExCONotInitializedException(Throwable cause) {
        super(cause);
    }

    public ExCONotInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

}
