/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class VNFComponent implements Serializable{
    /**
     * Unique VNFC identification within the namespace of a specific VNF.
     * */
    @Id
	protected String id;
    @Version
    protected int version = 0;

    public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
     * Describes network connectivity between a VNFC instance (based on this VDU) and an internal Virtual Link.
     * */
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    protected Set<VNFDConnectionPoint> connection_point;

    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }

    public VNFComponent() {
        this.connection_point = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<VNFDConnectionPoint> getConnection_point() {
        return connection_point;
    }

    public void setConnection_point(Set<VNFDConnectionPoint> connection_point) {
        this.connection_point = connection_point;
    }

    @Override
    public String toString() {
        return "VNFComponent{" +
                "connection_point=" + connection_point +
                ", id='" + id + '\'' +
                ", version=" + version +
                '}';
    }
}
