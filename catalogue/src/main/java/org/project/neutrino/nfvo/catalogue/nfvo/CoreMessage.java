package org.project.neutrino.nfvo.catalogue.nfvo;

import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;

import java.io.Serializable;

/**
 * Created by lorenzo on 5/30/15.
 */
public class CoreMessage implements Serializable{
    private Action action;
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    private VnfmManagerEndpoint vnfmManagerEndpoint;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    @Override
    public String toString() {
        return "CoreMessage{" +
                "action=" + action +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                ", vnfmManagerEndpoint=" + vnfmManagerEndpoint +
                '}';
    }

    public VnfmManagerEndpoint getVnfmManagerEndpoint() {
        return vnfmManagerEndpoint;
    }

    public void setVnfmManagerEndpoint(VnfmManagerEndpoint vnfmManagerEndpoint) {
        this.vnfmManagerEndpoint = vnfmManagerEndpoint;
    }

}
