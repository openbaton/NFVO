package org.project.neutrino.nfvo.catalogue.nfvo;

import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Created by lto on 01/07/15.
 */
@Entity
public class EventEndpoint {
    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    private String name;
    private EndpointType type;
    private String endpoint;
    private Action event;

    public Action getEvent() {
        return event;
    }

    public void setEvent(Action event) {
        this.event = event;
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

    public EndpointType getType() {
        return type;
    }

    public void setType(EndpointType type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "EventEndpoint{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", endpoint='" + endpoint + '\'' +
                ", event=" + event +
                '}';
    }

}
