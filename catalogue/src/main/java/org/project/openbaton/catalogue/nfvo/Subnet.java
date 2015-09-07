package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by mpa on 28.05.15.
 */
@Entity
public class Subnet implements Serializable {
    @Id
    private String id;
    @Version
    private int version = 0;
    private String name;
    private String extId;
    private String networkId;
    private String cidr;

    public Subnet() {
    }
    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }
    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() { return version; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    @Override
    public String toString() {
        return "Subnet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version +
                ", extId='" + extId + '\'' +
                ", networkId='" + networkId + '\'' +
                ", cidr='" + cidr + '\'' +
                '}';
    }
}
