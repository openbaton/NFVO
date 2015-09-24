package org.project.openbaton.common.vnfm_sdk.exception;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

/**
 * Created by mob on 11.08.15.
 */
public class VnfmSdkException extends Exception {
    private VirtualNetworkFunctionRecord vnfr;

    public VnfmSdkException() {
        super();
        vnfr = null;
    }

    public VnfmSdkException(String message) {
        super(message);
        vnfr = null;
    }

    public VnfmSdkException(String message, Throwable cause) {
        super(message, cause);
        vnfr = null;
    }

    public VnfmSdkException(Throwable e) {
        super(e);
        vnfr = null;
    }

    public VnfmSdkException(String message, VirtualNetworkFunctionRecord vnfr) {
        this.vnfr = vnfr;
    }

    public VirtualNetworkFunctionRecord getVnfr() {
        return vnfr;
    }

    public void setVnfr(VirtualNetworkFunctionRecord vnfr) {
        this.vnfr = vnfr;
    }
}
