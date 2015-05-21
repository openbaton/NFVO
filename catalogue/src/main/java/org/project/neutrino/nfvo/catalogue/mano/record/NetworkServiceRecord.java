/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.record;

import org.project.neutrino.nfvo.catalogue.mano.common.*;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VNFForwardingGraph;
import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkServiceRecord /*implements Serializable*/{
    @Id
    private String id = IdGenerator.createUUID();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AutoScalePolicy> auto_scale_policy;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConnectionPoint> connection_point;
    /**
     * Monitoring parameter used in this instance.
     * */
    @ElementCollection
     private List<String> monitoring_parameter;
    /**
     * References the nsd:service_deployment_flavour used to instantiate this Network Service instance.
     * */

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private ServiceDeploymentFlavour service_deployment_flavour;
    private String vendor;
    private String version;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VirtualLinkRecord> vlr;
    @OneToMany (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VirtualNetworkFunctionRecord> vnfr;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LifecycleEvent> lifecycle_event;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VNFDependency> vnf_dependency;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VNFForwardingGraph> vnffgr;
    /**
     * At least one
     * */

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PhysicalNetworkFunctionRecord> pnfr;
    /**
     * The reference to the Network Service Descriptor used to instantiate this Network Service.
     * */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     private NFVEntityDescriptor descriptor_reference;
    /**
     * Resource reservation information identification (potentially per individual VIM) for NFVI resources reserved for this NS instance.
     * TODO: find an appropriate type for this field
     * */
    private String resource_reservation;
    /**
     * Generic placeholder for input information related to NS orchestration and management policies to be applied
     * during runtime of a specific NS instance (e.g. for NS prioritization, etc.).
     * TODO: find an appropriate type for this field
     * */
    private String runtime_policy_info;
    /**
     * Flag to report status of the Network Service.
     * */

    @Enumerated(EnumType.STRING)
     private Status status;
    /**
     * System that has registered to received notifications of status changes
     * */
    private String notification;
    /**
     * Record of significant Network Service lifecycle events.
     * */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LifecycleEvent> lifecycle_event_history;
    /**
     * Record of detailed operational events.
     * TODO: maybe a pointer to a file?
     * */
    private String audit_log;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<String> getMonitoring_parameter() {
        return monitoring_parameter;
    }

    public void setMonitoring_parameter(List<String> monitoring_parameter) {
        this.monitoring_parameter = monitoring_parameter;
    }

    public ServiceDeploymentFlavour getService_deployment_flavour() {
        return service_deployment_flavour;
    }

    public void setService_deployment_flavour(ServiceDeploymentFlavour service_deployment_flavour) {
        this.service_deployment_flavour = service_deployment_flavour;
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

    public List<VirtualLinkRecord> getVlr() {
        return vlr;
    }

    public void setVlr(List<VirtualLinkRecord> vlr) {
        this.vlr = vlr;
    }

    public List<VirtualNetworkFunctionRecord> getVnfr() {
        return vnfr;
    }

    public void setVnfr(ArrayList<VirtualNetworkFunctionRecord> vnfr) {
        this.vnfr = vnfr;
    }

    public List<LifecycleEvent> getLifecycle_event() {
        return lifecycle_event;
    }

    public void setLifecycle_event(List<LifecycleEvent> lifecycle_event) {
        this.lifecycle_event = lifecycle_event;
    }

    public List<VNFDependency> getVnf_dependency() {
        return vnf_dependency;
    }

    public void setVnf_dependency(List<VNFDependency> vnf_dependency) {
        this.vnf_dependency = vnf_dependency;
    }

    public List<VNFForwardingGraph> getVnffgr() {
        return vnffgr;
    }

    public void setVnffgr(List<VNFForwardingGraph> vnffgr) {
        this.vnffgr = vnffgr;
    }

    public List<PhysicalNetworkFunctionRecord> getPnfr() {
        return pnfr;
    }

    public void setPnfr(List<PhysicalNetworkFunctionRecord> pnfr) {
        this.pnfr = pnfr;
    }

    public NFVEntityDescriptor getDescriptor_reference() {
        return descriptor_reference;
    }

    public void setDescriptor_reference(NFVEntityDescriptor descriptor_reference) {
        this.descriptor_reference = descriptor_reference;
    }

    public String getResource_reservation() {
        return resource_reservation;
    }

    public void setResource_reservation(String resource_reservation) {
        this.resource_reservation = resource_reservation;
    }

    public String getRuntime_policy_info() {
        return runtime_policy_info;
    }

    public void setRuntime_policy_info(String runtime_policy_info) {
        this.runtime_policy_info = runtime_policy_info;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
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

    public NetworkServiceRecord() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
