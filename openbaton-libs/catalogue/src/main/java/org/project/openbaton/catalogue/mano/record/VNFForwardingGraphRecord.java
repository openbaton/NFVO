/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.record;

import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.NetworkForwardingPath;
import org.project.openbaton.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
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
public class VNFForwardingGraphRecord implements Serializable{
    @Id
    private String id = IdGenerator.createUUID();
    /**
     * Record of the VNFFGD (vnffgd:id) used to instantiate this VNFFG
     * */

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private VNFForwardingGraphDescriptor descriptor_reference;
    /**
     * Reference to the record (nsr:id) for Network Service instance that this VNFFG instance is part of
     * */

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private NetworkServiceRecord parent_ns;
    /**
     * Reference to record for Virtual Link instance (vlr:id) used to instantiate this VNFFG
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VirtualLinkRecord> dependent_virtual_link;
    /**
     * Flag to report status of the VNFFG (e.g. 0=Failed, 1= normal operation, 2= degraded operation, 3= Offline
     * through management action)
     * */

    @Enumerated(EnumType.STRING)
    private Status status;
    /**
     * Listing of systems that have registered to received notifications of status changes
     * */

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> notification;
    /**
     * Record of significant VNFFG lifecycle events (e.g. creation, configuration changes)
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<LifecycleEvent> lifecycle_event_history;
    /**
     * Record of detailed operational events, (e.g. graph up/down, alarms sent)
     * */
    private String audit_log;
    /**
     * Set of Connection Points which form a Network Forwarding Path and description of policies to establish and rules
     * to choose the path
     * */

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private NetworkForwardingPath network_forwarding_path;
    /**
     * Reference to Connection Points (nsr/vnfr/pnfr:connection_point:id) forming the VNFFG
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VNFDConnectionPoint> connection_point;

    /**
     * VNF instance used to instantiate this VNFFG
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VirtualNetworkFunctionRecord> member_vnfs;
    private String vendor;
    private String version;
    private int number_of_endpoints;
    private int number_of_vnfs;
    private int number_of_pnfs;
    private int number_of_virtual_links;

    public VNFForwardingGraphRecord() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VNFForwardingGraphDescriptor getDescriptor_reference() {
        return descriptor_reference;
    }

    public void setDescriptor_reference(VNFForwardingGraphDescriptor descriptor_reference) {
        this.descriptor_reference = descriptor_reference;
    }

    public NetworkServiceRecord getParent_ns() {
        return parent_ns;
    }

    public void setParent_ns(NetworkServiceRecord parent_ns) {
        this.parent_ns = parent_ns;
    }

    public Set<VirtualLinkRecord> getDependent_virtual_link() {
        return dependent_virtual_link;
    }

    public void setDependent_virtual_link(Set<VirtualLinkRecord> dependent_virtual_link) {
        this.dependent_virtual_link = dependent_virtual_link;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<String> getNotification() {
        return notification;
    }

    public void setNotification(Set<String> notification) {
        this.notification = notification;
    }

    public Set<LifecycleEvent> getLifecycle_event_history() {
        return lifecycle_event_history;
    }

    public void setLifecycle_event_history(Set<LifecycleEvent> lifecycle_event_history) {
        this.lifecycle_event_history = lifecycle_event_history;
    }

    public String getAudit_log() {
        return audit_log;
    }

    public void setAudit_log(String audit_log) {
        this.audit_log = audit_log;
    }

    public NetworkForwardingPath getNetwork_forwarding_path() {
        return network_forwarding_path;
    }

    public void setNetwork_forwarding_path(NetworkForwardingPath network_forwarding_path) {
        this.network_forwarding_path = network_forwarding_path;
    }

    public Set<VNFDConnectionPoint> getConnection_point() {
        return connection_point;
    }

    public void setConnection_point(Set<VNFDConnectionPoint> connection_point) {
        this.connection_point = connection_point;
    }

    public Set<VirtualNetworkFunctionRecord> getMember_vnfs() {
        return member_vnfs;
    }

    public void setMember_vnfs(Set<VirtualNetworkFunctionRecord> member_vnfs) {
        this.member_vnfs = member_vnfs;
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

    public int getNumber_of_endpoints() {
        return number_of_endpoints;
    }

    public void setNumber_of_endpoints(int number_of_endpoints) {
        this.number_of_endpoints = number_of_endpoints;
    }

    public int getNumber_of_vnfs() {
        return number_of_vnfs;
    }

    public void setNumber_of_vnfs(int number_of_vnfs) {
        this.number_of_vnfs = number_of_vnfs;
    }

    public int getNumber_of_pnfs() {
        return number_of_pnfs;
    }

    public void setNumber_of_pnfs(int number_of_pnfs) {
        this.number_of_pnfs = number_of_pnfs;
    }

    public int getNumber_of_virtual_links() {
        return number_of_virtual_links;
    }

    public void setNumber_of_virtual_links(int number_of_virtual_links) {
        this.number_of_virtual_links = number_of_virtual_links;
    }
}