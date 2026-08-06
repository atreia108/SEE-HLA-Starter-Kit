package org.see.skf.callbacks;

final class NameReservationException extends RuntimeException {

    public NameReservationException(String message) {
        super(message);
    }

    public NameReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
