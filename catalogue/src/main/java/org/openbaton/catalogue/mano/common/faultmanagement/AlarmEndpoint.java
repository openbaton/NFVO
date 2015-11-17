package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 27.10.15.
 */

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import java.io.Serializable;

@Entity
public class AlarmEndpoint implements Serializable {
    @Id
    private String id;
    @Version
    private int version = 0;

    private String name;

    private String virtualNetworkFunctionId;

    private EndpointType type;
    private String endpoint;

    private PerceivedSeverity perceivedSeverity;

    public PerceivedSeverity getPerceivedSeverity() {
        return perceivedSeverity;
    }



    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public String getVirtualNetworkFunctionId() {
        return virtualNetworkFunctionId;
    }

    public void setVirtualNetworkFunctionId(String virtualNetworkFunctionId) {
        this.virtualNetworkFunctionId = virtualNetworkFunctionId;
    }
    public void setPerceivedSeverity(PerceivedSeverity perceivedSeverity) {
        this.perceivedSeverity = perceivedSeverity;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EndpointType getType() {
        return type;
    }

    public void setType(EndpointType type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "EventEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", virtualNetworkFunctionId='" + virtualNetworkFunctionId + '\'' +
                ", type=" + type +
                ", endpoint='" + endpoint + '\'' +
                '}';

    }
}
