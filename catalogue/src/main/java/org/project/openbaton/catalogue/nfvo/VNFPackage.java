package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by lto on 22/07/15.
 */
@Entity
public class VNFPackage {

    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    private String name;

    private String extId;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private NFVImage image;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Script> scripts;

    public VNFPackage() {
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

    public Set<Script> getScripts() {
        return scripts;
    }

    public void setScripts(Set<Script> scripts) {
        this.scripts = scripts;
    }

    @Override
    public String toString() {
        return "VNFPackage{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", extId='" + extId + '\'' +
                ", image=" + image +
                ", scripts=" + scripts +
                '}';
    }

    public void setImage(NFVImage image) {
        this.image = image;
    }

    public NFVImage getImage() {
        return image;
    }
}
