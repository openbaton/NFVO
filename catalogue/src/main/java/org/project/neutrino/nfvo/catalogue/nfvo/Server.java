package org.project.neutrino.nfvo.catalogue.nfvo;

import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

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
    private String extId;
    private String ip;

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

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
}
