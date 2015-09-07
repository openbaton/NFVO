package org.project.openbaton.catalogue.nfvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lto on 23/07/15.
 */
@Entity
public class Script implements Serializable{
    @Id
    private String id;
    @Version
    private int version = 0;

    private String name;

    @Lob
    @JsonIgnore
    private byte[] payload;

    public Script() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
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

    @JsonIgnore
    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Script{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                '}';
    }
}
