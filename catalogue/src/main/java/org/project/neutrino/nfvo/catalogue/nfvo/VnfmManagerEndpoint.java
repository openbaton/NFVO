package org.project.neutrino.nfvo.catalogue.nfvo;

import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

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
    private String endpoinType;

    public VnfmManagerEndpoint() {
    }

    public VnfmManagerEndpoint(String type, String endpoint, String endpoinType) {
        this.type = type;
        this.endpoint = endpoint;
        this.endpoinType = endpoinType;
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

    public String getEndpointType() {
        return endpoinType;
    }

    public void setEndpoinType(String endpoinType) {
        this.endpoinType = endpoinType;
    }

    @Override
    public String toString() {
        return "VnfmManagerEndpoint{" +
                "type='" + type + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", endpoinType='" + endpoinType + '\'' +
                '}';
    }
}
