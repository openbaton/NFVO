package org.project.openbaton.catalogue.nfvo;

import javax.persistence.Entity;

/**
 * Created by lto on 27/05/15.
 */
@Entity
public class VnfmManagerEndpoint extends Endpoint{



    public VnfmManagerEndpoint() {
    }

    public VnfmManagerEndpoint(String type, String endpoint, EndpointType endpointType) {
        this.type = type;
        this.endpoint = endpoint;
        this.endpointType = endpointType;
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
