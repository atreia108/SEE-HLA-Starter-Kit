package org.see.skf.internal.callbacks;

public final class NameReservationException extends RuntimeException {

    public NameReservationException(Throwable cause) {
        super(cause);
    }

    public NameReservationException(String message) {
        super(message);
    }

    public NameReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
