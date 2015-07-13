package org.project.openbaton.nfvo.common.exceptions;

/**
 * Created by lto on 08/06/15.
 */
public class BadFormatException extends Throwable {
    public BadFormatException(String s) {
        super(s);
    }
    public BadFormatException(Throwable t){
        super(t);
    }
}
