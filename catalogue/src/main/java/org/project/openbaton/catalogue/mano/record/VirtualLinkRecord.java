/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.record;

import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 * <p/>
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VirtualLinkRecord implements Serializable {
    @Id
    private String id;
    @Version
    private int hb_version = 0;
    private String vendor;
    private String version;
    private int number_of_enpoints;
    private String root_requirement;
    private String leaf_requirement;

    /**
     * extId of the network to attach
     */
    private String extId;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> qos;
    /**
     * Test access facilities available on the VL (e.g. none, passive monitoring, or active (intrusive) loopbacks at endpoints
     * TODO think of using Enum instead of String
     */
    private String test_access;
    /**
     * A reference to an attached Connection Point (nsd/vnfd/pnfd:connection_point:id)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> connection;
    /**
     * The reference for the Network Service instance (nsr:id) that this VL instance is part of
     */
    private String parent_ns;

    /**
     * References to the records of the VNFFG instances in which this VL instance participates
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VNFForwardingGraphRecord> vnffgr_reference;
    /**
     * Reference to the id of VLD used to instantiate this VL
     */
    private String descriptor_reference;
    /**
     * The reference to the system managing this VL instance
     */
    private String vim_id;
    /**
     * Bandwidth allocated for each of the QoS options on this link
     */

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> allocated_capacity;
    /**
     * Flag to report status of the VL (e.g. 0=Link down, 1= normal operation, 2= degraded operation, 3= Offline through management action)
     */

    @Enumerated(EnumType.STRING)
    private LinkStatus status;

    /**
     * System that has registered to received notifications of status changes
     * TODO consider a notification framework
     */

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> notification;
    /**
     * Record of significant VL lifecycle event (e.g. Creation, Configuration changes)
     */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<LifecycleEvent> lifecycle_event_history;
    /**
     * Record of detailed operational events (e.g. link up/down, Operator logins, Alarms sent)
     * TODO consider a stream to a file
     */

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> audit_log;
    /**
     * Connectivity types, e.g. E-Line, E-LAN, or E-Tree
     * TODO consider a Enum
     */
    private String connectivity_type;

    public VirtualLinkRecord() {
    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public String getId() {
        return id;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
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

    public int getNumber_of_enpoints() {
        return number_of_enpoints;
    }

    public void setNumber_of_enpoints(int number_of_enpoints) {
        this.number_of_enpoints = number_of_enpoints;
    }

    public String getRoot_requirement() {
        return root_requirement;
    }

    public void setRoot_requirement(String root_requirement) {
        this.root_requirement = root_requirement;
    }

    public String getLeaf_requirement() {
        return leaf_requirement;
    }

    public void setLeaf_requirement(String leaf_requirement) {
        this.leaf_requirement = leaf_requirement;
    }

    public Set<String> getQos() {
        return qos;
    }

    public void setQos(Set<String> qos) {
        this.qos = qos;
    }

    public String getTest_access() {
        return test_access;
    }

    public void setTest_access(String test_access) {
        this.test_access = test_access;
    }

    public Set<String> getConnection() {
        return connection;
    }

    public void setConnection(Set<String> connection) {
        this.connection = connection;
    }

    public String getParent_ns() {
        return parent_ns;
    }

    public void setParent_ns(String parent_ns) {
        this.parent_ns = parent_ns;
    }

    public Set<VNFForwardingGraphRecord> getVnffgr_reference() {
        return vnffgr_reference;
    }

    public void setVnffgr_reference(Set<VNFForwardingGraphRecord> vnffgr_reference) {
        this.vnffgr_reference = vnffgr_reference;
    }

    public String getDescriptor_reference() {
        return descriptor_reference;
    }

    public void setDescriptor_reference(String descriptor_reference) {
        this.descriptor_reference = descriptor_reference;
    }

    public int getHb_version() {
        return hb_version;
    }

    public void setHb_version(int hb_version) {
        this.hb_version = hb_version;
    }

    public String getVim_id() {
        return vim_id;
    }

    public void setVim_id(String vim_id) {
        this.vim_id = vim_id;
    }

    public Set<String> getAllocated_capacity() {
        return allocated_capacity;
    }

    public void setAllocated_capacity(Set<String> allocated_capacity) {
        this.allocated_capacity = allocated_capacity;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public void setStatus(LinkStatus status) {
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

    public Set<String> getAudit_log() {
        return audit_log;
    }

    public void setAudit_log(Set<String> audit_log) {
        this.audit_log = audit_log;
    }

    public String getConnectivity_type() {
        return connectivity_type;
    }

    public void setConnectivity_type(String connectivity_type) {
        this.connectivity_type = connectivity_type;
    }
}
