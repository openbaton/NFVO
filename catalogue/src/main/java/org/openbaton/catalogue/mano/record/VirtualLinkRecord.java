/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.catalogue.mano.record;

import org.openbaton.catalogue.mano.common.AbstractVirtualLink;
import org.openbaton.catalogue.mano.common.LifecycleEvent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 * <p>
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VirtualLinkRecord extends AbstractVirtualLink implements Serializable {
    //    @Id
//    private String id;
//    @Version
//    private int hb_version = 0;
    private String vendor;
    private String version;
    private int number_of_endpoints;

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
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> connection;
    public VirtualLinkRecord() {
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

    public Set<String> getConnection() {
        return connection;
    }

    public void setConnection(Set<String> connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        return "VirtualLinkRecord{" +
                "vendor='" + vendor + '\'' +
                ", version='" + version + '\'' +
                ", name='" + getName() + '\'' +
                ", extId='" + getExtId() + '\'' +
                ", number_of_endpoints=" + number_of_endpoints +
                ", parent_ns='" + parent_ns + '\'' +
                ", vnffgr_reference=" + vnffgr_reference +
                ", descriptor_reference='" + descriptor_reference + '\'' +
                ", vim_id='" + vim_id + '\'' +
                ", allocated_capacity=" + allocated_capacity +
                ", status=" + status +
                ", notification=" + notification +
                ", lifecycle_event_history=" + lifecycle_event_history +
                ", audit_log=" + audit_log +
                ", connection=" + connection +
                '}';
    }
}
