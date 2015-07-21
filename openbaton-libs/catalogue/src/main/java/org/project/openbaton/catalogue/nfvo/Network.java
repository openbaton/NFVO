package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 20/05/15.
 */
@Entity
public class Network implements Serializable {
    @Id
    private String id = IdGenerator.createUUID();;
    @Version
    private static int version = 0;
    private String name;
    private String extId;
    private Boolean external = false;
    private Boolean shared = false;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Subnet> subnets;

    public Network(){
    }

    @Override
    public String toString() {
        return "Network{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", extId='" + extId + '\'' +
                ", external=" + external +
                ", shared=" + shared +
                ", subnets=" + subnets +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static int getVersion() {
        return version;
    }

    public static void setVersion(int version) {
        Network.version = version;
    }

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

    public Boolean isExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public Boolean isShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public Set<Subnet> getSubnets() {
        return subnets;
    }

    public void setSubnets(Set<Subnet> subnets) {
        this.subnets = subnets;
    }
}
