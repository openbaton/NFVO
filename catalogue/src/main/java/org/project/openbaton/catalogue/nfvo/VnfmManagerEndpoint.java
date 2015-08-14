package org.project.openbaton.catalogue.nfvo;

import javax.persistence.Entity;

/**
 * Created by lto on 27/05/15.
 */
@Entity
public class VnfmManagerEndpoint extends Endpoint{


    private String endpoint;


    public VnfmManagerEndpoint() {
    }

    public VnfmManagerEndpoint(String type, String endpoint, EndpointType endpointType) {
        this.type = type;
        this.endpoint = endpoint;
        this.endpointType = endpointType;
    }


    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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
