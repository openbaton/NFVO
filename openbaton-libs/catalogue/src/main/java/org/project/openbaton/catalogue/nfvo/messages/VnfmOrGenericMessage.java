package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/**
 * Created by mob on 14.09.15.
 */
public class VnfmOrGenericMessage implements VnfmOrMessage {
    private Action action;
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    private VNFRecordDependency vnfRecordDependency;



    public VnfmOrGenericMessage(VirtualNetworkFunctionRecord vnfr, Action action) {
        this.virtualNetworkFunctionRecord = vnfr;
        this.action=action;

    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord vnfr) {
        this.virtualNetworkFunctionRecord = vnfr;
    }
    public VNFRecordDependency getVnfRecordDependency() {
        return vnfRecordDependency;
    }

    public void setVnfRecordDependency(VNFRecordDependency vnfRecordDependency) {
        this.vnfRecordDependency = vnfRecordDependency;
    }

    @Override
    public String toString() {
        return "VnfmOrGenericMessage{" +
                "action=" + action +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                ", vnfRecordDependency=" + vnfRecordDependency +
                '}';
    }

    @Override
    public Action getAction() {
        return action;
    }
}
