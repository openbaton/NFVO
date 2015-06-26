package org.project.neutrino.nfvo.sdk.api.exception;

import java.lang.Exception;
import java.lang.Throwable;


/**
* OpenBaton SDK base Exception. Thrown to the caller of the library while using requester's functions.
*/
public class SDKException extends Exception {

    /**
     * Creates an sdk exeption without exception message
     */
    public SDKException () {}

    /**
     * Creates an sdk exeption with an exception message as string
     *
     * @param message:
     *               the exceptions custom message
     */
    public SDKException (String message) {
        super(message);
    }

    /**
     * Creates an sdk exeption with a throwable cause
     *
     * @param cause:
     *               the throwable cause
     */
    public SDKException (Throwable cause) {
        super(cause);
    }

    /**
     * Creates an sdk exeption with a message as string and a throwable cause
     *
     * @param message:
     *               the exceptions custom message
     *
     * @param cause:
     *               the throwable cause
     */
    public SDKException (String message, Throwable cause) {
        super(message, cause);
    }
}