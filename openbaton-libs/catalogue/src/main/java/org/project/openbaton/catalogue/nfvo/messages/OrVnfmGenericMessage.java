package org.project.openbaton.catalogue.nfvo.messages;

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by mob on 14.09.15.
 */
public class OrVnfmGenericMessage implements OrVnfmMessage {
    private Action action;
    private VirtualNetworkFunctionRecord vnfr;
    private VNFRecordDependency vnfrd;

    public OrVnfmGenericMessage(VirtualNetworkFunctionRecord vnfr, Action action) {
        this.vnfr = vnfr;
        this.action=action;
    }
    public VNFRecordDependency getVnfrd() {
        return vnfrd;
    }

    public void setVnfrd(VNFRecordDependency vnfrd) {
        this.vnfrd = vnfrd;
    }

    public VirtualNetworkFunctionRecord getVnfr() {
        return vnfr;
    }

    public void setVnfr(VirtualNetworkFunctionRecord vnfr) {
        this.vnfr = vnfr;
    }

    @Override
    public String toString() {
        return "OrVnfmGenericMessage{" +
                "vnfr=" + vnfr +
                '}';
    }

    @Override
    public Action getAction() {
        return null;
    }
}
