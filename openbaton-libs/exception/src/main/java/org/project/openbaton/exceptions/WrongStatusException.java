package org.project.openbaton.exceptions;

/**
 * Created by lto on 04/08/15.
 */
public class WrongStatusException extends Exception{
    public WrongStatusException(String s) {
        super(s);
    }

    public WrongStatusException() {
    }

    public WrongStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongStatusException(Throwable cause) {
        super(cause);
    }
}
