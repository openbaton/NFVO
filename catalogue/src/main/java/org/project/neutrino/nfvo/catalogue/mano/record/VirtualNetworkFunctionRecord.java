/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.record;

import org.project.neutrino.nfvo.catalogue.mano.common.AutoScalePolicy;
import org.project.neutrino.nfvo.catalogue.mano.common.ConnectionPoint;
import org.project.neutrino.nfvo.catalogue.mano.common.LifecycleEvent;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDependency;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.InternalVirtualLink;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VNFDDeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by lto on 06/02/15.
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VirtualNetworkFunctionRecord {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AutoScalePolicy> auto_scale_policy;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConnectionPoint> connection_point;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VNFDependency> dependency;
    /**
     * Reference to selected deployment flavour (vnfd:deployment_flavour:id)
     * */

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private VNFDDeploymentFlavour deployment_flavour;
    /**
     * ID of the VNF instance
     * */

    @Id
    private String id = IdGenerator.createUUID();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LifecycleEvent> lifecycle_event;
    /**
     * A language attribute may be specified to identifydefault localisation/language
     * */
    private String localization;
    /**
     * Active monitoring parameters
     * */

    @ElementCollection
    private List<String> monitoring_parameter;
    /**
     * VDU elements describing the VNFC-related relevant information, see clause @VirtualDeploymentUnit
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VirtualDeploymentUnit> vdu;

    private String vendor;
    private String version;
    /**
     * Internal Virtual Links instances used in this VNF
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InternalVirtualLink> virtual_link;
    /**
     * Reference to records of Network Service instances (nsr:id) that this VNF instance is part of
     * */

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private NetworkServiceRecord parent_ns;
    /**
     * The reference to the VNFD used to instantiate this VNF
     * */
    private String descriptor_reference;
    /**
     * The identification of the VNFM entity managing this VNF
     * TODO probably it is better to have a reference than a string pointing to the id
     * */
    private String vnfm_id;

    /**
     * Reference to a VLR (vlr:id) used for the management access path or other internal and external connection
     * interface configured for use by this VNF instance
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VirtualLinkRecord> connected_external_virtual_link;
    /**
     * A network address (e.g. VLAN, IP) configured for the management access or other internal and external connection
     * interface on this VNF
     * */

    @ElementCollection
    private List<String> vnf_address;
    /**
     * Flag to report status of the VNF (e.g. 0=Failed, 1= normal operation, 2= degraded operation, 3= offline through
     * management action)
     * */

    @Enumerated(EnumType.STRING)
    private Status status;
    /**
     * Listing of systems that have registered to received notifications of status changes
     * TODO maybe passing to a notification framework
     * */

    @ElementCollection
    private List<String> notification;
    /**
     * Record of significant VNF lifecycle event (e.g. creation, scale up/down, configuration changes)
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LifecycleEvent> lifecycle_event_history;
    /**
     * Record of detailed operational event, (e.g. VNF boot, operator logins, alarms sent)
     * */
    private String audit_log;
    /**
     * Generic placeholder for input information related to VNF orchestration and management policies to be applied
     * during runtime of a specific VNF instance (e.g. for VNF prioritization, etc.)
     * */
    @ElementCollection
    private List<String> runtime_policy_info;

    public VirtualNetworkFunctionRecord() {
    }

    public List<AutoScalePolicy> getAuto_scale_policy() {
        return auto_scale_policy;
    }

    public void setAuto_scale_policy(List<AutoScalePolicy> auto_scale_policy) {
        this.auto_scale_policy = auto_scale_policy;
    }

    public List<ConnectionPoint> getConnection_point() {
        return connection_point;
    }

    public void setConnection_point(List<ConnectionPoint> connection_point) {
        this.connection_point = connection_point;
    }

    public List<VNFDependency> getDependency() {
        return dependency;
    }

    public void setDependency(List<VNFDependency> dependency) {
        this.dependency = dependency;
    }

    public VNFDDeploymentFlavour getDeployment_flavour() {
        return deployment_flavour;
    }

    public void setDeployment_flavour(VNFDDeploymentFlavour deployment_flavour) {
        this.deployment_flavour = deployment_flavour;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LifecycleEvent> getLifecycle_event() {
        return lifecycle_event;
    }

    public void setLifecycle_event(List<LifecycleEvent> lifecycle_event) {
        this.lifecycle_event = lifecycle_event;
    }

    public String getLocalization() {
        return localization;
    }

    public void setLocalization(String localization) {
        this.localization = localization;
    }

    public List<String> getMonitoring_parameter() {
        return monitoring_parameter;
    }

    public void setMonitoring_parameter(List<String> monitoring_parameter) {
        this.monitoring_parameter = monitoring_parameter;
    }

    public List<VirtualDeploymentUnit> getVdu() {
        return vdu;
    }

    public void setVdu(List<VirtualDeploymentUnit> vdu) {
        this.vdu = vdu;
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

    public List<InternalVirtualLink> getVirtual_link() {
        return virtual_link;
    }

    public void setVirtual_link(List<InternalVirtualLink> virtual_link) {
        this.virtual_link = virtual_link;
    }

    public NetworkServiceRecord getParent_ns() {
        return parent_ns;
    }

    public void setParent_ns(NetworkServiceRecord parent_ns) {
        this.parent_ns = parent_ns;
    }

    public String getDescriptor_reference() {
        return descriptor_reference;
    }

    public void setDescriptor_reference(String descriptor_reference) {
        this.descriptor_reference = descriptor_reference;
    }

    public String getVnfm_id() {
        return vnfm_id;
    }

    public void setVnfm_id(String vnfm_id) {
        this.vnfm_id = vnfm_id;
    }

    public List<VirtualLinkRecord> getConnected_external_virtual_link() {
        return connected_external_virtual_link;
    }

    public void setConnected_external_virtual_link(List<VirtualLinkRecord> connected_external_virtual_link) {
        this.connected_external_virtual_link = connected_external_virtual_link;
    }

    public List<String> getVnf_address() {
        return vnf_address;
    }

    public void setVnf_address(List<String> vnf_address) {
        this.vnf_address = vnf_address;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<String> getNotification() {
        return notification;
    }

    public void setNotification(List<String> notification) {
        this.notification = notification;
    }

    public List<LifecycleEvent> getLifecycle_event_history() {
        return lifecycle_event_history;
    }

    public void setLifecycle_event_history(List<LifecycleEvent> lifecycle_event_history) {
        this.lifecycle_event_history = lifecycle_event_history;
    }

    public String getAudit_log() {
        return audit_log;
    }

    public void setAudit_log(String audit_log) {
        this.audit_log = audit_log;
    }

    public List<String> getRuntime_policy_info() {
        return runtime_policy_info;
    }

    public void setRuntime_policy_info(List<String> runtime_policy_info) {
        this.runtime_policy_info = runtime_policy_info;
    }
}
