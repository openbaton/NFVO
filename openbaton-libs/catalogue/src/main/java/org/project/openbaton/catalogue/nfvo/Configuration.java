package org.project.openbaton.catalogue.nfvo;

/**
 * Created by lto on 18/05/15.
 */

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
public class Configuration implements Serializable{
    @Id
    private String id;
    @Version
    private int version;

    // TODO think at cascade type
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ConfigurationParameter> configurationParameters;

    @Override
    public String toString() {
        return "Configuration{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", configurationParameters=" + configurationParameters +
                ", name='" + name + '\'' +
                '}';
    }

    private String name;
    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
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

    public Set<ConfigurationParameter> getConfigurationParameters() {
        return configurationParameters;
    }

    public void setConfigurationParameters(Set<ConfigurationParameter> configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
