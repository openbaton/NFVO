package org.project.openbaton.catalogue.mano.record;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
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

    @OneToOne(cascade = {CascadeType.REFRESH/*, CascadeType.MERGE*/}, fetch = FetchType.EAGER)
    private VirtualNetworkFunctionRecord source;
    @OneToOne(cascade = {CascadeType.REFRESH/*, CascadeType.MERGE*/}, fetch = FetchType.EAGER)
    private VirtualNetworkFunctionRecord target;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> parameters;
    private Status status;

    public VNFRecordDependency() {
    }

    @Override
    public String toString() {
        return "VNFRecordDependency{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", source=" + source.getName() + "(" + source.getId() + ")" +
                ", target=" + target + "(" + target.getId() + ")" +
                ", parameters=" + parameters +
                '}';
    }

    public Set<String> getParameters() {
        return parameters;
    }

    public void setParameters(Set<String> parameters) {
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

    public VirtualNetworkFunctionRecord getSource() {
        return source;
    }

    public void setSource(VirtualNetworkFunctionRecord source) {
        this.source = source;
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
