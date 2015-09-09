package org.project.openbaton.common.vnfm_sdk.exception;

/**
 * Created by mob on 31.08.15.
 */
public class BadFormatException extends Throwable {
    public BadFormatException(String s) {
        super(s);
    }
    public BadFormatException(Throwable t){
        super(t);
    }
}
