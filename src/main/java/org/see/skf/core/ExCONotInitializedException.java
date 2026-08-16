package org.see.skf.core;

final class ExCONotInitializedException extends RuntimeException {

    ExCONotInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

    ExCONotInitializedException(Throwable cause) {
        super(cause);
    }

}
