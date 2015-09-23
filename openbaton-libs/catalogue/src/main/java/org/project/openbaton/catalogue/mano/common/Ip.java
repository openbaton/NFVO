package org.project.openbaton.catalogue.mano.common;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by lto on 23/09/15.
 */
@Entity
public class Ip implements Serializable{
    @Id
    private String id;
    @Version
    private int version = 0;

    private String netName;
    private String ip;

    @Override
    public String toString() {
        return "Ip{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", netName='" + netName + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }
}
