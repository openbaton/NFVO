package org.project.openbaton.nfvo.common.exceptions;

/**
 * Created by lto on 26/05/15.
 */
public class NotFoundException extends Exception {
    public NotFoundException(String msg){
        super(msg);
    }

    public NotFoundException(Throwable e) {
        super(e);
    }
}
