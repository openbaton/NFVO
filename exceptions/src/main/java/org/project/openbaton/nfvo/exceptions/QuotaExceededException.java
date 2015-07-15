package org.project.openbaton.nfvo.exceptions;

/**
 * Created by lto on 08/06/15.
 */
public class QuotaExceededException extends Throwable {
    public QuotaExceededException(String s) {
        super(s);
    }
    public QuotaExceededException(Throwable t){
        super(t);
    }
}
