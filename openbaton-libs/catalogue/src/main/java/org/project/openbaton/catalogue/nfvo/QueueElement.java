package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;

import java.util.List;

/**
 * Created by lto on 19/08/15.
 */
public class QueueElement {

    private String vnfrTargetId;

    private List<VNFRecordDependency> dependencies;

    private int waitingFor;

    public String getVnfrTargetId() {
        return vnfrTargetId;
    }

    public void setVnfrTargetId(String vnfrTargetId) {
        this.vnfrTargetId = vnfrTargetId;
    }

    public List<VNFRecordDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<VNFRecordDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public int getWaitingFor() {
        return waitingFor;
    }

    public void setWaitingFor(int waitingFor) {
        this.waitingFor = waitingFor;
    }

    @Override
    public String toString() {
        return "QueueElement{" +
                "vnfrTargetId='" + vnfrTargetId + '\'' +
                ", dependencies=" + dependencies +
                ", waitingFor=" + waitingFor +
                '}';
    }
}
