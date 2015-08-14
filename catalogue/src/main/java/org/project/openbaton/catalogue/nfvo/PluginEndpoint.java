package org.project.openbaton.catalogue.nfvo;

import javax.persistence.Entity;

/**
 * Created by lto on 13/08/15.
 */
@Entity
public class PluginEndpoint extends Endpoint{

    @Override
    public String toString() {
        return "VnfmManagerEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", type='" + type + '\'' +
                ", endpointType='" + endpointType + '\'' +
                '}';
    }
}
