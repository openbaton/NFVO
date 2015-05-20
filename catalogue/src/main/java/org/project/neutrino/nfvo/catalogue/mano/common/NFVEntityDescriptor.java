package org.project.neutrino.nfvo.catalogue.mano.common;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.VNFForwardingGraph;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.project.neutrino.nfvo.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class NFVEntityDescriptor /*implements Serializable*/ {

	/**
	 * ID of this Network Service Descriptor
	 * */
	@Id
	protected String id = IdGenerator.createUUID();
	@Version
	protected Integer hb_version = 0;
	/**
	 * Provider or vendor of the Network Service.
	 * */
	protected String vendor;
	/**
	 * Version of the Network Service Descriptor.
	 * */
	protected String version;
	/**
	 * VNFFG which is part of the Network Service, see clause 6.5.1.
	 * A Network Service might have multiple graphs, for example, for:
	 *  1. Control plane traffic.
	 *  2. Management-plane traffic.
	 *  3. User plane traffic itself could have multiple NFPs based on the QOS etc. The traffic is steered amongst 1 of these NFPs based on the policy decisions.
	 *
	 **/
	@OneToMany(cascade = CascadeType.ALL)
	protected List<VNFForwardingGraph> vnffgd;
	@OneToMany(cascade = CascadeType.ALL)
	protected List<VirtualLinkDescriptor> vld;
	@OneToMany(cascade = CascadeType.ALL)
	protected List<LifecycleEvent> lifecycle_event;
	/**
	 *
	 * Represents a monitoring parameter which can be tracked for this NS. These can be network service metrics that are tracked for the
	 * purpose of meeting the network service availability contributing to SLAs (e.g. NS downtime). These can also be used for specifying different deployment
	 * flavours for the Network Service in Network Service Descriptor, and/or to indicate different levels of network service availability.
	 * Examples include specific parameters such as calls-per-second (cps), number-of-subscribers, no-of-rules, flows-per-second, etc.
	 * 1 or more of these parameters could be influential in determining the need to scale-out.
	 *
	 * TODO: check if the String is the appropriate type for this field
	 *
	 * */
	@ElementCollection(fetch = FetchType.EAGER)
	protected List<String> monitoring_parameter;
	/**
	*
	* Represents the service KPI parameters and its requirement for each deployment flavour of the NS being described,
	* see clause 6.2.1.3. For example, there could be a flavour describing the requirements to support a vEPC with 300k calls
	* per second. There could be another flavour describing the requirements to support a vEPC with 500k calls per second.
	*
	* */
	@OneToMany(cascade = CascadeType.ALL)
	protected List<DeploymentFlavour> service_deployment_flavour;
	@OneToMany(cascade = CascadeType.ALL)
	protected List<AutoScalePolicy> auto_scale_policy;
	/**
	 * This element describes a Connection Point which acts as an endpoint of the Network Service, see clause 6.2.1.2.
	 * This can, for example, be referenced by other elements as an
	 * endpoint.
	 * */
	@OneToMany(cascade = CascadeType.ALL)
	protected List<ConnectionPoint> connection_point;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getHb_version() {
		return hb_version;
	}
	public void setHb_version(Integer hb_version) {
		this.hb_version = hb_version;
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
	public List<VNFForwardingGraph> getVnffgd() {
		return vnffgd;
	}
	public void setVnffgd(List<VNFForwardingGraph> vnffgd) {
		this.vnffgd = vnffgd;
	}
	public List<VirtualLinkDescriptor> getVld() {
		return vld;
	}
	public void setVld(List<VirtualLinkDescriptor> vld) {
		this.vld = vld;
	}
	public List<LifecycleEvent> getLifecycle_event() {
		return lifecycle_event;
	}
	public void setLifecycle_event(List<LifecycleEvent> lifecycle_event) {
		this.lifecycle_event = lifecycle_event;
	}
	public List<String> getMonitoring_parameter() {
		return monitoring_parameter;
	}
	public void setMonitoring_parameter(List<String> monitoring_parameter) {
		this.monitoring_parameter = monitoring_parameter;
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
	public List<DeploymentFlavour> getService_deployment_flavour() {
		return service_deployment_flavour;
	}
	public void setService_deployment_flavour(
			List<DeploymentFlavour> service_deployment_flavour) {
		this.service_deployment_flavour = service_deployment_flavour;
	}
	
	

}