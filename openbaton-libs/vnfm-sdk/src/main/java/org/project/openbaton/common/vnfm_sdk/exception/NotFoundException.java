package org.project.openbaton.common.vnfm_sdk.exception;

/**
 * Created by mob on 31.08.15.
 */
public class NotFoundException extends Exception {
    public NotFoundException(String msg){
        super(msg);
    }

    public NotFoundException(Throwable e) {
        super(e);
    }
}
