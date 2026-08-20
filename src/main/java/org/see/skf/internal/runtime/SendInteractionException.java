package org.see.skf.internal.runtime;

final class SendInteractionException extends RuntimeException {

    SendInteractionException(String message) {
        super(message);
    }

    SendInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
