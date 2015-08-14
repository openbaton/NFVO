package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lto on 20/05/15.
 */
@Entity
public class Server implements Serializable{
    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;
    private String name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private NFVImage image;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private DeploymentFlavour flavor;

    private String status;
    private String extendedStatus;
    private String extId;
    private HashMap<String,List<String>> ips;
    private String floatingIp;

    @Temporal(TemporalType.DATE)
    private Date created;
    @Temporal(TemporalType.DATE)
    private Date updated;

    public String getId() { return id; }

    public int getVersion() { return version; }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getExtId() {
        return extId;
    }

    public void setIps(HashMap<String,List<String>> ips) {
        this.ips = ips;
    }

    public HashMap<String,List<String>> getIps() {
        return ips;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getExtendedStatus() {
        return extendedStatus;
    }

    public void setExtendedStatus(String extendedStatus) {
        this.extendedStatus = extendedStatus;
    }

    public NFVImage getImage() {
        return image;
    }

    public void setImage(NFVImage image) {
        this.image = image;
    }

    public DeploymentFlavour getFlavor() {
        return flavor;
    }

    public void setFlavor(DeploymentFlavour flavor) {
        this.flavor = flavor;
    }

    public String getFloatingIp() { return floatingIp; }

    public void setFloatingIp(String floatingIp) { this.floatingIp = floatingIp; }

    @Override
    public String toString() {
        return "Server{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", extendedStatus='" + status + '\'' +
                ", version=" + version +
                ", extId='" + extId + '\'' +
                ", ips='" + ips + '\'' +
                ", floatingIp='" + floatingIp + '\'' +
                ", created='" + created + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}

