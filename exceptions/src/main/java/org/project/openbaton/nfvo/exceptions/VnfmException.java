package org.project.openbaton.nfvo.exceptions;

/**
 * Created by lto on 23/06/15.
 */
public class VnfmException extends Exception {
    public VnfmException() {
        super();
    }

    public VnfmException(String message) {
        super(message);
    }

    public VnfmException(String message, Throwable cause) {
        super(message, cause);
    }

    public VnfmException(Throwable cause) {
        super(cause);
    }
}
