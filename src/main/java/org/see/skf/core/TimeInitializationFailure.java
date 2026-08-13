package org.see.skf.core;

public final class TimeInitializationFailure extends RuntimeException {

    public TimeInitializationFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeInitializationFailure(Throwable e) {
        super(e);
    }
}
