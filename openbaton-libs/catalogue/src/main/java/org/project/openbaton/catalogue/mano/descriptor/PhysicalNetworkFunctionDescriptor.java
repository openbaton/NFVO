/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.mano.common.ConnectionPoint;
import org.project.openbaton.catalogue.mano.common.Security;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 */
@Entity
public class PhysicalNetworkFunctionDescriptor implements Serializable{

    /**
     * The ID (e.g. name) of this PNFD.
     * */
    @Id
	private String id;
    @Version
    private int hb_version = 0;
    /**
     *The vendor generating this PNFD.
     * */
    private String vendor;
    /**
     * The version of PNF this PNFD is describing.
     * */
    private String version;
    /**
     * Description of physical device functionality.
     * */
    private String description;
    /**
     * This element describes an external interface exposed by this PNF enabling connection with a VL.
     * */
    @OneToMany(cascade = CascadeType.ALL)	
    private Set<ConnectionPoint> connection_point;
    /**
     * Version of the PNF descriptor.
     * */
    private String descriptor_version;
    /**
     * This is a signature of pnfd to prevent tampering. The particular hash algorithm used to compute the signature,
     * together with the corresponding cryptographic certificate to validate the signature should also be included.
     * */
    @OneToOne(cascade = CascadeType.ALL)
    private Security pnfd_security;
    @PrePersist
    public void ensureId(){
        id=IdGenerator.createUUID();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ConnectionPoint> getConnection_point() {
        return connection_point;
    }

    public void setConnection_point(Set<ConnectionPoint> connection_point) {
        this.connection_point = connection_point;
    }

    public String getDescriptor_version() {
        return descriptor_version;
    }

    public void setDescriptor_version(String descriptor_version) {
        this.descriptor_version = descriptor_version;
    }

    public Security getPnfd_security() {
        return pnfd_security;
    }

    public void setPnfd_security(Security pnfd_security) {
        this.pnfd_security = pnfd_security;
    }

    public PhysicalNetworkFunctionDescriptor() {

    }

	@Override
	public String toString() {
		return "PhysicalNetworkFunctionDescriptor [id=" + id + ", hb_version="
				+ hb_version + ", vendor=" + vendor + ", version=" + version
				+ ", description=" + description + ", connection_point="
				+ connection_point + ", descriptor_version="
				+ descriptor_version + ", pnfd_security=" + pnfd_security + "]";
	}
}
