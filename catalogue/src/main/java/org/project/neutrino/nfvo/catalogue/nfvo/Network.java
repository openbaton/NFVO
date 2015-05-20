package org.project.neutrino.nfvo.catalogue.nfvo;

import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 20/05/15.
 */
@Entity
public class Network {
    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;
    private String name;
    private String extId;
    private String networkType;
    private Boolean external;
    private Boolean shared;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> subnets;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getExtId() {
        return extId;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setSubnets(Iterable<String> subnets) {
        List<String> tmp = new ArrayList<String>();
        for (String s : subnets){
            tmp.add(s);
        }
        this.subnets = tmp;
    }

    public Iterable<String> getSubnets() {
        return subnets;
    }
}
