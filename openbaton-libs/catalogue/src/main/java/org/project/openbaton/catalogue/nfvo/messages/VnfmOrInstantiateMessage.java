package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/**
 * Created by mob on 14.09.15.
 */
public class VnfmOrInstantiateMessage implements VnfmOrMessage {

    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    public VnfmOrInstantiateMessage(VirtualNetworkFunctionRecord vnfr) {

        this.virtualNetworkFunctionRecord = vnfr;
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord vnfr) {
        this.virtualNetworkFunctionRecord = vnfr;
    }

    @Override
     public String toString() {
        return "VnfmOrInstantiateMessage{" +
                "vnfr=" + virtualNetworkFunctionRecord +
                '}';
    }

    @Override
    public Action getAction() {
        return Action.INSTANTIATE;
    }
}
