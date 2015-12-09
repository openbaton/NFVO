package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by moritz on 02.12.15.
 */
public class OrVnfmHealVNFRequestMessage extends OrVnfmMessage {
    private VNFCInstance vnfcInstance;
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    private String cause;

    public VNFCInstance getVnfcInstance() {
        return vnfcInstance;
    }

    public void setVnfcInstance(VNFCInstance vnfcInstance) {
        this.vnfcInstance = vnfcInstance;
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "OrVnfmHealVNFRequestMessage{" +
                "vnfcInstance=" + vnfcInstance +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                ", cause='" + cause + '\'' +
                '}';
    }
}
