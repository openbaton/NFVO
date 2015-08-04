/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.record;

import org.project.openbaton.catalogue.mano.common.ConnectionPoint;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class PhysicalNetworkFunctionRecord implements Serializable{

    @Id
    private String id = IdGenerator.createUUID();
    private String vendor;
    private String version;
    private String description;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ConnectionPoint> connection_point;
    /**
     * The reference for the record of the NS instance (nsr:id) that this PNF instance is part of
     * */
     private String  parent_ns_id;
    /**
     * The reference to the version of PNFD (pnfd:version) used to instantiate this PNF
     * */

    private String descriptor_reference;
    /**
     * References to the records of VNFFG instances to which this PNF is participating
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VNFForwardingGraphRecord> vnffgr;
    /**
     * The reference to the system managing this PNF
     * */
    private String oam_reference;
    /**
     * References to the VLRs (vlr:id) used to for the management access path and all other external connection
     * interfaces configured for use by this PNF instance
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VirtualLinkRecord> connected_virtual_link;
    /**
     * The network addresses (e.g. VLAN, IP) configured for the management access and all other external connection
     * interfaces on this PNF
     * */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> pnf_address;

    public PhysicalNetworkFunctionRecord() {
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

    public String  getParent_ns_id() {
        return parent_ns_id;
    }

    public void setParent_ns_id(String  parent_ns_id) {
        this.parent_ns_id = parent_ns_id;
    }

    public String getDescriptor_reference() {
        return descriptor_reference;
    }

    public void setDescriptor_reference(String  descriptor_reference) {
        this.descriptor_reference = descriptor_reference;
    }

    public Set<VNFForwardingGraphRecord> getVnffgr() {
        return vnffgr;
    }

    public void setVnffgr(Set<VNFForwardingGraphRecord> vnffgr) {
        this.vnffgr = vnffgr;
    }

    public String getOam_reference() {
        return oam_reference;
    }

    public void setOam_reference(String oam_reference) {
        this.oam_reference = oam_reference;
    }

    public Set<VirtualLinkRecord> getConnected_virtual_link() {
        return connected_virtual_link;
    }

    public void setConnected_virtual_link(Set<VirtualLinkRecord> connected_virtual_link) {
        this.connected_virtual_link = connected_virtual_link;
    }

    public Set<String> getPnf_address() {
        return pnf_address;
    }

    public void setPnf_address(Set<String> pnf_address) {
        this.pnf_address = pnf_address;
    }

}
