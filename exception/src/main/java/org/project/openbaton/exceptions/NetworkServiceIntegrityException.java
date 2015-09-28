package org.project.openbaton.exceptions;

/**
 * Created by lto on 25/09/15.
 */
public class NetworkServiceIntegrityException extends Exception{
    public NetworkServiceIntegrityException() {
    }

    public NetworkServiceIntegrityException(Throwable cause) {
        super(cause);
    }

    public NetworkServiceIntegrityException(String message) {
        super(message);
    }

    public NetworkServiceIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkServiceIntegrityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
