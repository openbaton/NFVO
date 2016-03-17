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

package org.openbaton.catalogue.mano.descriptor;

import org.openbaton.catalogue.mano.common.HighAvailability;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 06/02/15.
 * <p/>
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class VirtualDeploymentUnit implements Serializable {
    @Id
    private String id;
    @Version
    private int version = 0;

    /**
     * A unique identifier of this VDU within the scope
     * of the VNFD, including version functional
     * * description and other identification information.
     * This will be used to refer to VDU when defining
     * relationships between them.
     */
    private String name;
    /**
     * This provides a reference to a VM image
     * NOTE: A cardinality of zero allows for creating empty virtualisation containers as per (ETSI GS NFV-SWA 001 [i.8]).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> vm_image;



    /**
     * Describe the required computation resources characteristics (e.g. processing power, number of virtual CPUs, etc.),
     * including Key Quality Indicators (KQIs) for performance and reliability/availability.
     */
    private String computation_requirement;
    /**
     * This represents the virtual memory needed for the VDU.
     */
    private String virtual_memory_resource_element;
    /**
     * This represents the requirements in terms of the virtual network bandwidth needed for the VDU.
     */
    private String virtual_network_bandwidth_resource;
    /**
     * Defines VNF component functional scripts/workflows for specific lifecycle events(e.g. initialization, termination,
     * graceful shutdown, scaling out/in).
     */
    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    private Set<LifecycleEvent> lifecycle_event;
    /**
     * Placeholder for other constraints.
     */
    private String vdu_constraint;
    /**
     * Defines redundancy model to ensure high availability examples include: ActiveActive: Implies that two instance of
     * the same VDU will co-exists with continuous data synchronization. ActivePassive: Implies that two instance of
     * the same VDU will co-exists without any data synchronization.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL )
    private HighAvailability high_availability;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private Set<VRFaultManagementPolicy> fault_management_policy;
    /**
     * Defines minimum and maximum number of instances which can be created to support scale out/in.
     */
    private int scale_in_out;
    /**
     * Contains information that is distinct for each VNFC created based on this VDU.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<VNFComponent> vnfc;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<VNFCInstance> vnfc_instance;

    /**
     * Monitoring parameter, which can be tracked for a VNFC based on this VDU. Examples include: memory-consumption,
     * CPU-utilisation, bandwidth-consumption, VNFC downtime, etc.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> monitoring_parameter;

    private String hostname;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> vimInstanceName;

    public VirtualDeploymentUnit() {
    }

    public Set<VNFCInstance> getVnfc_instance() {
        return vnfc_instance;
    }

    public void setVnfc_instance(Set<VNFCInstance> vnfc_instance) {
        this.vnfc_instance = vnfc_instance;
    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Set<String> getVm_image() {
        return vm_image;
    }

    public void setVm_image(Set<String> vm_image) {
        this.vm_image = vm_image;
    }

    public String getComputation_requirement() {
        return computation_requirement;
    }

    public void setComputation_requirement(String computation_requirement) {
        this.computation_requirement = computation_requirement;
    }

    public String getVirtual_memory_resource_element() {
        return virtual_memory_resource_element;
    }

    public void setVirtual_memory_resource_element(String virtual_memory_resource_element) {
        this.virtual_memory_resource_element = virtual_memory_resource_element;
    }

    public String getVirtual_network_bandwidth_resource() {
        return virtual_network_bandwidth_resource;
    }

    public void setVirtual_network_bandwidth_resource(String virtual_network_bandwidth_resource) {
        this.virtual_network_bandwidth_resource = virtual_network_bandwidth_resource;
    }

    public Set<LifecycleEvent> getLifecycle_event() {
        return lifecycle_event;
    }

    public void setLifecycle_event(Set<LifecycleEvent> lifecycle_event) {
        this.lifecycle_event = lifecycle_event;
    }

    public String getVdu_constraint() {
        return vdu_constraint;
    }

    public void setVdu_constraint(String vdu_constraint) {
        this.vdu_constraint = vdu_constraint;
    }

    public HighAvailability getHigh_availability() {
        return high_availability;
    }

    public void setHigh_availability(HighAvailability high_availability) {
        this.high_availability = high_availability;
    }

    public int getScale_in_out() {
        return scale_in_out;
    }

    public void setScale_in_out(int scale_in_out) {
        this.scale_in_out = scale_in_out;
    }

    public Set<VNFComponent> getVnfc() {
        return vnfc;
    }

    public void setVnfc(Set<VNFComponent> vnfc) {
        this.vnfc = vnfc;
    }

    public Set<String> getMonitoring_parameter() {
        return monitoring_parameter;
    }

    @Override
    public String toString() {
        return "VirtualDeploymentUnit{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", vm_image=" + vm_image +
                ", computation_requirement='" + computation_requirement + '\'' +
                ", virtual_memory_resource_element='" + virtual_memory_resource_element + '\'' +
                ", virtual_network_bandwidth_resource='" + virtual_network_bandwidth_resource + '\'' +
                ", lifecycle_event=" + lifecycle_event +
                ", vdu_constraint='" + vdu_constraint + '\'' +
                ", high_availability=" + high_availability +
                ", fault_management_policy=" + fault_management_policy +
                ", scale_in_out=" + scale_in_out +
                ", vnfc=" + vnfc +
                ", vnfc_instance=" + vnfc_instance +
                ", monitoring_parameter=" + monitoring_parameter +
                ", hostname='" + hostname + '\'' +
                ", vimInstanceName='" + vimInstanceName + '\'' +
                '}';
    }

    public Set<VRFaultManagementPolicy> getFault_management_policy() {
        return fault_management_policy;
    }

    public void setFault_management_policy(Set<VRFaultManagementPolicy> fault_management_policy) {
        this.fault_management_policy = fault_management_policy;
    }

    public void setMonitoring_parameter(Set<String> monitoring_parameter) {
        this.monitoring_parameter = monitoring_parameter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getVimInstanceName() {
        return vimInstanceName;
    }

    public void setVimInstanceName(List<String> vimInstanceName) {
        this.vimInstanceName = vimInstanceName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}
