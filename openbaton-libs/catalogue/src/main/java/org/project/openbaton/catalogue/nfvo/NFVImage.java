package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lto on 11/05/15.
 */
@Entity
public class NFVImage implements Serializable {
    @Id
    private String id;
    @Version
    private int version = 0;

    private String extId;
    private String name;
    private long minRam; //in MB
    private long minDiskSpace; //in GB
    private String minCPU;

    private boolean isPublic;
    private String diskFormat;
    private String containerFormat;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    public NFVImage() {
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
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

    public long getMinRam() {
        return minRam;
    }

    public void setMinRam(long minRam) {
        this.minRam = minRam;
    }

    public long getMinDiskSpace() {
        return minDiskSpace;
    }

    public void setMinDiskSpace(long minDiskSpace) {
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

    public boolean isPublic() { return isPublic; }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getDiskFormat() {
        return diskFormat;
    }

    public void setDiskFormat(String diskFormat) {
        this.diskFormat = diskFormat;
    }

    public String getContainerFormat() {
        return containerFormat;
    }

    public void setContainerFormat(String containerFormat) {
        this.containerFormat = containerFormat;
    }

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
                ", public='" + isPublic + '\'' +
                ", diskFormat='" + diskFormat + '\'' +
                ", containerFormat='" + containerFormat + '\'' +
                ", created='" + created + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}
