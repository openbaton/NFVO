package org.openbaton.exceptions;

/**
 * Created by lto on 16/02/16.
 */
public class AlreadyExistingException extends Exception {
    public AlreadyExistingException(Throwable cause) {
        super(cause);
    }

    public AlreadyExistingException(String message) {
        super(message);
    }

    public AlreadyExistingException(String message, Throwable cause) {
        super(message, cause);
    }
}
