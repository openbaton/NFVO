package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by lto on 27/05/15.
 */
@Entity
public class VnfmManagerEndpoint implements Serializable{

    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    private String type;
    private String endpoint;
    private EndpointType endpointType;

    public VnfmManagerEndpoint() {
    }

    public VnfmManagerEndpoint(String type, String endpoint, EndpointType endpointType) {
        this.type = type;
        this.endpoint = endpoint;
        this.endpointType = endpointType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public EndpointType getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "VnfmManagerEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", type='" + type + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", endpointType='" + endpointType + '\'' +
                '}';
    }
}
