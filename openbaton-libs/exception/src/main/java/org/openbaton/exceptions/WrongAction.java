package org.openbaton.exceptions;

/**
 * Created by lto on 22/02/16.
 */
public class WrongAction extends Exception {
    public WrongAction(Throwable cause) {
        super(cause);
    }

    public WrongAction(String message) {
        super(message);
    }

    public WrongAction(String message, Throwable cause) {
        super(message, cause);
    }
}
