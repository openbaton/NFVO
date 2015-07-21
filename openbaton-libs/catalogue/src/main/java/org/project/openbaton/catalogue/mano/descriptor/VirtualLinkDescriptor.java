/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.mano.common.AbstractVirtualLink;
import org.project.openbaton.catalogue.mano.common.Security;

import java.util.Set;

import javax.persistence.*;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VirtualLinkDescriptor extends AbstractVirtualLink {
	
	/**
     * Vendor generating this VLD
     * */
    private String vendor;
    /**
     * Version of this VLD
     * */
    private String descriptor_version;
    /**
     * Number of endpoints available on this VL (e.g. E-Line=2)
     * */
    private int number_of_endpoints;
    /**
     * A reference to an attached Connection Point (nsd/vnfd/pnfd:connection_point:id)
     * */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> connection;
    /**
     * This is a signature of vld to prevent tampering. The particular hash algorithm used to compute the signature,
     * together with the corresponding cryptographic certificate to validate the signature should also be included
     * */
    @OneToOne(cascade=CascadeType.ALL)
    private Security vld_security;

    public VirtualLinkDescriptor() {
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDescriptor_version() {
        return descriptor_version;
    }

    public void setDescriptor_version(String descriptor_version) {
        this.descriptor_version = descriptor_version;
    }

    public int getNumber_of_endpoints() {
        return number_of_endpoints;
    }

    public void setNumber_of_endpoints(int number_of_endpoints) {
        this.number_of_endpoints = number_of_endpoints;
    }

    public Set<String> getConnection() {
        return connection;
    }

    public void setConnection(Set<String> connection) {
        this.connection = connection;
    }

    public Security getVld_security() {
        return vld_security;
    }

    public void setVld_security(Security vld_security) {
        this.vld_security = vld_security;
    }
}
