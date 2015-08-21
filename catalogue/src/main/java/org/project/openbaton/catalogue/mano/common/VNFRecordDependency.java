package org.project.openbaton.catalogue.mano.common;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by lto on 08/06/15.
 */
@Entity
public class VNFRecordDependency implements Serializable {

    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    @OneToMany(cascade = {CascadeType.REFRESH/*, CascadeType.MERGE*/}, fetch = FetchType.EAGER)
    private Set<VirtualNetworkFunctionRecord> sources;

    @OneToOne(cascade = {CascadeType.REFRESH/*, CascadeType.MERGE*/}, fetch = FetchType.EAGER)
    private VirtualNetworkFunctionRecord target;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String ,Set<String>> parameters;
    private Status status;

    public VNFRecordDependency() {
    }

    @Override
    public String toString() {
        String srcs = "";
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : sources) {
            srcs += virtualNetworkFunctionRecord.getName() + "(" + virtualNetworkFunctionRecord.getId() + "), ";
        }
        srcs = srcs.substring(0,srcs.length() - 3);
        return "VNFRecordDependency{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", sources={" + srcs + "}" +
                ", target=" + target + "(" + target.getId() + ")" +
                ", parameters=" + parameters +
                '}';
    }

    public Map<String ,Set<String>> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String ,Set<String>> parameters) {
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Set<VirtualNetworkFunctionRecord> getSources() {
        return sources;
    }

    public void setSources(Set<VirtualNetworkFunctionRecord> sources) {
        this.sources = sources;
    }

    public VirtualNetworkFunctionRecord getTarget() {
        return target;
    }

    public void setTarget(VirtualNetworkFunctionRecord target) {
        this.target = target;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
