package org.see.skf.core;

public class PublishException extends Exception {

    PublishException(String message) {
        super(message);
    }

    PublishException(String message, Throwable cause) {
        super(message, cause);
    }

    PublishException(Throwable cause) {
        super(cause);
    }
}
