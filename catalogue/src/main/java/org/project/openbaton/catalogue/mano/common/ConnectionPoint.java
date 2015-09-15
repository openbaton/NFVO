package org.project.openbaton.catalogue.mano.common;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lto on 06/02/15.
 *
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ConnectionPoint implements Serializable{

    /*ID of the Connection Point.*/
	@Id
    protected String id;
	@Version
	protected int version = 0;

    /**
     *
     * This may be for example a virtual port, a virtual NIC address, a physical port, a physical NIC address or the
     * endpoint of an IP VPN enabling network connectivity.
     * TODO think about what type must be
     *
     * */
    protected String type;

    public ConnectionPoint() {
    }

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }
    public String getType() {

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ConnectionPoint{" +
                "id='" + id + '\'' +
                ", version='" + version +
                ", type='" + type + '\'' +
                '}';
    }

}
