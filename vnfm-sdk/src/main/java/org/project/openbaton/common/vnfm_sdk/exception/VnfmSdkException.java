package org.project.openbaton.common.vnfm_sdk.exception;

/**
 * Created by mob on 11.08.15.
 */
public class VnfmSdkException extends Exception {
    public VnfmSdkException() {
        super();
    }

    public VnfmSdkException(String message) {
        super(message);
    }

    public VnfmSdkException(String message, Throwable cause) {
        super(message, cause);
    }

    public VnfmSdkException(Throwable e) {
        super(e);
    }
}
