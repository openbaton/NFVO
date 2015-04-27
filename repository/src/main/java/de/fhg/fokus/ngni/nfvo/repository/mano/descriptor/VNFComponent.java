/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package de.fhg.fokus.ngni.nfvo.repository.mano.descriptor;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import de.fhg.fokus.ngni.nfvo.repository.util.IdGenerator;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VNFComponent {
    /**
     * Unique VNFC identification within the namespace of a specific VNF.
     * */
    @Id
	private String id=IdGenerator.createUUID();
    @Version
    private int version = 0;
    public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
     * Describes network connectivity between a VNFC instance (based on this VDU) and an internal Virtual Link.
     * */
	@OneToMany(cascade=CascadeType.ALL)
    private List<VNFDConnectionPoint> connection_point;

    public VNFComponent() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<VNFDConnectionPoint> getConnection_point() {
        return connection_point;
    }

    public void setConnection_point(List<VNFDConnectionPoint> connection_point) {
        this.connection_point = connection_point;
    }
}
