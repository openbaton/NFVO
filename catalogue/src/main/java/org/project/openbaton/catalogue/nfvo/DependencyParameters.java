package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by lto on 21/08/15.
 */
@Entity
public class DependencyParameters implements Serializable{

    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, String> parameters;

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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "DependencyParameters{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", parameters=" + parameters +
                '}';
    }
}
