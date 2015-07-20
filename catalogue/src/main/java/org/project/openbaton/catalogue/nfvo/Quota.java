package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;

/**
 * Created by lto on 20/05/15.
 */
@Entity
public class Quota {
    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    private String tenant;

    private int cores;
    private int floatingIps;
    private int instances;
    private int keyPairs;
    private int ram;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public int getKeyPairs() {
        return keyPairs;
    }

    public void setKeyPairs(int keyPairs) {
        this.keyPairs = keyPairs;
    }

    public int getInstances() { return instances; }

    public void setInstances(int instances) { this.instances = instances; }

    public int getFloatingIps() {
        return floatingIps;
    }

    public void setFloatingIps(int floatingIps) {
        this.floatingIps = floatingIps;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Quota{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", tenant='" + tenant + '\'' +
                ", cores='" + cores + '\'' +
                ", floatingIps='" + floatingIps + '\'' +
                ", instances='" + instances + '\'' +
                ", keypairs='" + keyPairs + '\'' +
                ", ram='" + ram + '\'' +
                '}';
    }
}

