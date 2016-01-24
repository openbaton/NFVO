package org.openbaton.exceptions;

/**
 * Created by lto on 15/01/16.
 */
public class CyclicDependenciesException extends Exception{
    public CyclicDependenciesException(Throwable cause) {
        super(cause);
    }

    public CyclicDependenciesException(String message) {
        super(message);
    }

    public CyclicDependenciesException(String message, Throwable cause) {
        super(message, cause);
    }
}
