package org.project.neutrino.nfvo.catalogue.nfvo;

import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lto on 11/05/15.
 */
@Entity
public class NFVImage implements Serializable {
    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    private String extId;
    private String name;
    private int minRam;
    private int minDiskSpace;
    private String minCPU;

    @Temporal(TemporalType.DATE)
    private Date created;

    @Temporal(TemporalType.DATE)
    private Date updated;

    public NFVImage() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public int getMinRam() {
        return minRam;
    }

    public void setMinRam(int minRam) {
        this.minRam = minRam;
    }

    public int getMinDiskSpace() {
        return minDiskSpace;
    }

    public void setMinDiskSpace(int minDiskSpace) {
        this.minDiskSpace = minDiskSpace;
    }

    public String getMinCPU() {
        return minCPU;
    }

    public void setMinCPU(String minCPU) {
        this.minCPU = minCPU;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() { return updated; }

    public void setUpdated(Date updated) { this.updated = updated; }

    @Override
    public String toString() {
        return "Image{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", extId='" + extId + '\'' +
                ", minRam='" + minRam + '\'' +
                ", minDiskSpace='" + minDiskSpace + '\'' +
                ", minCPU='" + minCPU + '\'' +
                ", created='" + created + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}
