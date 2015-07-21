/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.openbaton.catalogue.mano.descriptor;

import org.project.openbaton.catalogue.mano.common.HighAvailability;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.nfvo.VimInstance;
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
public class VirtualDeploymentUnit implements Serializable{
	@Id
    private String id = IdGenerator.createUUID();
	@Version
	private int version = 0;
    /**
     * This provides a reference to a VM image
     * NOTE: A cardinality of zero allows for creating empty virtualisation containers as per (ETSI GS NFV-SWA 001 [i.8]).
     * */
    @ElementCollection(fetch=FetchType.EAGER)
    private Set<String> vm_image;
    /**
     * Describe the required computation resources characteristics (e.g. processing power, number of virtual CPUs, etc.),
     * including Key Quality Indicators (KQIs) for performance and reliability/availability.
     * */
    private String computation_requirement;
    /**
     * This represents the virtual memory needed for the VDU.
     * */
    private String virtual_memory_resource_element;
    /**
     * This represents the requirements in terms of the virtual network bandwidth needed for the VDU.
     * */
    private String virtual_network_bandwidth_resource;
    /**
     * Defines VNF component functional scripts/workflows for specific lifecycle events(e.g. initialization, termination,
     * graceful shutdown, scaling out/in).
     * */
    @OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    private Set<LifecycleEvent> lifecycle_event;
    /**
     * Placeholder for other constraints.
     * */
    private String vdu_constraint;
    /**
     * Defines redundancy model to ensure high availability examples include: ActiveActive: Implies that two instance of
     * the same VDU will co-exists with continuous data synchronization. ActivePassive: Implies that two instance of
     * the same VDU will co-exists without any data synchronization.
     * */
    @Enumerated(EnumType.STRING)
    private HighAvailability high_availability;
    /**
     * Defines minimum and maximum number of instances which can be created to support scale out/in.
     * */
    private int scale_in_out;
    /**
     * Contains information that is distinct for each VNFC created based on this VDU.
     * */
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<VNFComponent> vnfc;
    /**
     * Monitoring parameter, which can be tracked for a VNFC based on this VDU. Examples include: memory-consumption,
     * CPU-utilisation, bandwidth-consumption, VNFC downtime, etc.
     * */
    @ElementCollection(fetch=FetchType.EAGER)
    private Set<String> monitoring_parameter;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH /*TODO sure about this?*/})
    private VimInstance vimInstance;
    private String hostname;

    private String extId;

    public VirtualDeploymentUnit() {
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

    public void setMonitoring_parameter(Set<String> monitoring_parameter) {
        this.monitoring_parameter = monitoring_parameter;
    }

    @Override
    public String toString() {
        return "VirtualDeploymentUnit{" +
                "id='" + id + '\'' +
                ", extId=" + extId +
                ", version=" + version +
                ", vm_image=" + vm_image +
                ", computation_requirement='" + computation_requirement + '\'' +
                ", virtual_memory_resource_element='" + virtual_memory_resource_element + '\'' +
                ", virtual_network_bandwidth_resource='" + virtual_network_bandwidth_resource + '\'' +
                ", lifecycle_event=" + lifecycle_event +
                ", vdu_constraint='" + vdu_constraint + '\'' +
                ", high_availability=" + high_availability +
                ", scale_in_out=" + scale_in_out +
                ", vnfc=" + vnfc +
                ", monitoring_parameter=" + monitoring_parameter +
                '}';
    }

    public VimInstance getVimInstance() {
        return vimInstance;
    }

    public void setVimInstance(VimInstance vimInstance) {
        this.vimInstance = vimInstance;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }
}
