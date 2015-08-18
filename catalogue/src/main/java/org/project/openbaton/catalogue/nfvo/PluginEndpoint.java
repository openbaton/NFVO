package org.project.openbaton.catalogue.nfvo;

import javax.persistence.Entity;

/**
 * Created by lto on 13/08/15.
 */
@Entity
public class PluginEndpoint extends Endpoint{

    private String interfaceClass;
    private String interfaceVersion;

    @Override
    public String toString() {
        return "PluginEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", type='" + type + '\'' +
                ", endpointType='" + endpointType + '\'' +
                '}';
    }

    public String getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(String interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setInterfaceVersion(String interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }

    public String getInterfaceVersion() {
        return interfaceVersion;
    }
}
