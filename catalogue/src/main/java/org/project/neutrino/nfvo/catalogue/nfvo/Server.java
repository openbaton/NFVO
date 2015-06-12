package org.project.neutrino.nfvo.catalogue.nfvo;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by lto on 20/05/15.
 */
@Entity
public class Server {
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
    private String ip;
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

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
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
                ", ip='" + ip + '\'' +
                ", floatingIp='" + floatingIp + '\'' +
                ", created='" + created + '\'' +
                ", updated='" + updated + '\'' +
                '}';
    }
}

