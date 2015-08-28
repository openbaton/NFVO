package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

import java.io.Serializable;

/**
 * Created by lorenzo on 5/30/15.
 */
public class CoreMessage implements Serializable{

    private Action action;
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    private VNFRecordDependency dependency;

    public VNFRecordDependency getDependency() {
        return dependency;
    }

    public void setDependency(VNFRecordDependency dependency) {
        this.dependency = dependency;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "CoreMessage{" +
                "action=" + action +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                ", dependency=" + dependency +
                '}';
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }
}
