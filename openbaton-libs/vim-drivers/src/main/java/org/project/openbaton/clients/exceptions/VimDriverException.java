package org.project.openbaton.clients.exceptions;

/**
 * Created by lto on 13/07/15.
 */
public class VimDriverException extends Exception {

    public VimDriverException() {
    }

    public VimDriverException(String message) {
        super(message);
    }

    public VimDriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
