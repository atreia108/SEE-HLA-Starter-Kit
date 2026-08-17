package org.see.skf.core;

final class ExCONotInitializedException extends RuntimeException {

    ExCONotInitializedException(String message) {
        super(message);
    }

    ExCONotInitializedException(Throwable cause) {
        super(cause);
    }

    public ExCONotInitializedException(String message, Throwable cause) {
        super(message, cause);
    }
}
