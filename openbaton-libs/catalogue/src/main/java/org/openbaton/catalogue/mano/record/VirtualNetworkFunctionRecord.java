/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
 *
 */

package org.openbaton.catalogue.mano.record;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import org.openbaton.catalogue.mano.common.AutoScalePolicy;
import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.HistoryLifecycleEvent;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by lto on 06/02/15. Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12) */
@Entity
public class VirtualNetworkFunctionRecord implements Serializable {

  /** ID of the VNF instance */
  @Id private String id;

  @Version private int hb_version = 0;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<AutoScalePolicy> auto_scale_policy;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<ConnectionPoint> connection_point;

  private String projectId;
  /** Reference to selected deployment flavour (vnfd:deployment_flavour_key:id) */
  private String deployment_flavour_key;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Configuration configurations;
  /**
   * Record of significant VNF lifecycle event (e.g. creation, scale up/down, configuration changes)
   */
  @OneToMany(
    cascade = {CascadeType.ALL},
    fetch = FetchType.EAGER,
    orphanRemoval = true
  )
  private Set<LifecycleEvent> lifecycle_event;

  @OneToMany(
    cascade = {CascadeType.ALL},
    fetch = FetchType.EAGER
  )
  private List<HistoryLifecycleEvent> lifecycle_event_history;
  /** A language attribute may be specified to identify default localisation/language */
  private String localization;
  /** Active monitoring parameters */
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> monitoring_parameter;
  /**
   * VDU elements describing the VNFC-related relevant information, see
   * clause @VirtualDeploymentUnit
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<VirtualDeploymentUnit> vdu;

  private String vendor;
  private String version;
  /** Internal Virtual Links instances used in this VNF */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<InternalVirtualLink> virtual_link;
  /** The nsr id */
  private String parent_ns_id;
  /** The reference to the VNFD used to instantiate this VNF */
  private String descriptor_reference;
  /**
   * The identification of the VNFM entity managing this VNF TODO probably it is better to have a
   * reference than a string pointing to the id
   */
  private String vnfm_id;
  /**
   * Reference to a VLR (vlr:id) used for the management access path or other internal and external
   * connection interface configured for use by this VNF instance
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<VirtualLinkRecord> connected_external_virtual_link;
  /**
   * A network address (e.g. VLAN, IP) configured for the management access or other internal and
   * external connection interface on this VNF
   */
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> vnf_address;
  /**
   * Flag to report status of the VNF (e.g. 0=Failed, 1= normal operation, 2= degraded operation, 3=
   * offline through management action)
   *
   * <p>Implementation thoughts: the states are defined in
   * http://www.etsi.org/deliver/etsi_gs/NFV-SWA/001_099/001/01.01.01_60/gs_NFV-SWA001v010101p.pdf
   * so for what concerns the VNFR, the state are:
   *
   * <p>* Null) A VNF Instance does not exist and is about to be created. * Instantiated Not
   * Configured) VNF Instance does exist but is not configured for service. * Instantiated
   * Configured - Inactive) A VNF Instance is configured for service. * Instantiated Configured -
   * Active) A VNF Instance that participates in service. * Terminated) A VNF Instance has ceased to
   * exist. but the Null and the Instantiated since when the VNFR is created will be ready to serve.
   */
  @Enumerated(EnumType.STRING)
  private Status status;
  /**
   * Listing of systems that have registered to received notifications of status changes TODO maybe
   * passing to a notification framework
   */
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> notification;
  /** Record of detailed operational event, (e.g. VNF boot, operator logins, alarms sent) */
  private String audit_log;
  /**
   * Generic placeholder for input information related to VNF orchestration and management policies
   * to be applied during runtime of a specific VNF instance (e.g. for VNF prioritization, etc.)
   */
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> runtime_policy_info;

  private String name;
  private String type;
  private String endpoint;
  private String task;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Configuration requires;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Configuration provides;

  @JsonIgnore private boolean cyclicDependency;
  private String packageId;

  public VirtualNetworkFunctionRecord() {
    this.lifecycle_event = new HashSet<>();
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public Configuration getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Configuration configurations) {
    this.configurations = configurations;
  }

  public boolean isCyclicDependency() {
    return cyclicDependency;
  }

  public void setCyclicDependency(boolean cyclicDependency) {
    this.cyclicDependency = cyclicDependency;
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public boolean hasCyclicDependency() {
    return cyclicDependency;
  }

  public String getParent_ns_id() {
    return parent_ns_id;
  }

  public void setParent_ns_id(String parent_ns_id) {
    this.parent_ns_id = parent_ns_id;
  }

  public Set<AutoScalePolicy> getAuto_scale_policy() {
    return auto_scale_policy;
  }

  public void setAuto_scale_policy(Set<AutoScalePolicy> auto_scale_policy) {
    this.auto_scale_policy = auto_scale_policy;
  }

  public int getHb_version() {
    return hb_version;
  }

  public void setHb_version(int hb_version) {
    this.hb_version = hb_version;
  }

  public Set<ConnectionPoint> getConnection_point() {
    return connection_point;
  }

  public void setConnection_point(Set<ConnectionPoint> connection_point) {
    this.connection_point = connection_point;
  }

  public String getDeployment_flavour_key() {
    return deployment_flavour_key;
  }

  public void setDeployment_flavour_key(String deployment_flavour_key) {
    this.deployment_flavour_key = deployment_flavour_key;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<LifecycleEvent> getLifecycle_event() {
    return lifecycle_event;
  }

  public void setLifecycle_event(Set<LifecycleEvent> lifecycle_event) {
    this.lifecycle_event = lifecycle_event;
  }

  public String getLocalization() {
    return localization;
  }

  public void setLocalization(String localization) {
    this.localization = localization;
  }

  public Set<String> getMonitoring_parameter() {
    return monitoring_parameter;
  }

  public void setMonitoring_parameter(Set<String> monitoring_parameter) {
    this.monitoring_parameter = monitoring_parameter;
  }

  public Set<VirtualDeploymentUnit> getVdu() {
    return vdu;
  }

  public void setVdu(Set<VirtualDeploymentUnit> vdu) {
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

  public Set<InternalVirtualLink> getVirtual_link() {
    return virtual_link;
  }

  public void setVirtual_link(Set<InternalVirtualLink> virtual_link) {
    this.virtual_link = virtual_link;
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

  public Set<VirtualLinkRecord> getConnected_external_virtual_link() {
    return connected_external_virtual_link;
  }

  public void setConnected_external_virtual_link(
      Set<VirtualLinkRecord> connected_external_virtual_link) {
    this.connected_external_virtual_link = connected_external_virtual_link;
  }

  public Set<String> getVnf_address() {
    return vnf_address;
  }

  public void setVnf_address(Set<String> vnf_address) {
    this.vnf_address = vnf_address;
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

  public List<HistoryLifecycleEvent> getLifecycle_event_history() {
    return lifecycle_event_history;
  }

  public void setLifecycle_event_history(List<HistoryLifecycleEvent> lifecycle_event_history) {
    this.lifecycle_event_history = lifecycle_event_history;
  }

  public String getAudit_log() {
    return audit_log;
  }

  public void setAudit_log(String audit_log) {
    this.audit_log = audit_log;
  }

  public Set<String> getRuntime_policy_info() {
    return runtime_policy_info;
  }

  public void setRuntime_policy_info(Set<String> runtime_policy_info) {
    this.runtime_policy_info = runtime_policy_info;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Configuration getRequires() {
    return requires;
  }

  public void setRequires(Configuration requires) {
    this.requires = requires;
  }

  public Configuration getProvides() {
    return provides;
  }

  public void setProvides(Configuration provides) {
    this.provides = provides;
  }

  public String getTask() {
    return task;
  }

  public void setTask(String task) {
    this.task = task;
  }

  public String getPackageId() {
    return packageId;
  }

  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }

  @Override
  public String toString() {
    return "VirtualNetworkFunctionRecord{"
        + "audit_log='"
        + audit_log
        + '\''
        + ", id='"
        + id
        + '\''
        + ", hb_version="
        + hb_version
        + ", auto_scale_policy="
        + auto_scale_policy
        + ", connection_point="
        + connection_point
        + ", deployment_flavour_key='"
        + deployment_flavour_key
        + '\''
        + ", configurations="
        + configurations
        + ", lifecycle_event="
        + lifecycle_event
        + ", lifecycle_event_history="
        + lifecycle_event_history
        + ", localization='"
        + localization
        + '\''
        + ", monitoring_parameter="
        + monitoring_parameter
        + ", vdu="
        + vdu
        + ", vendor='"
        + vendor
        + '\''
        + ", version='"
        + version
        + '\''
        + ", virtual_link="
        + virtual_link
        + ", parent_ns_id='"
        + parent_ns_id
        + '\''
        + ", descriptor_reference='"
        + descriptor_reference
        + '\''
        + ", vnfm_id='"
        + vnfm_id
        + '\''
        + ", connected_external_virtual_link="
        + connected_external_virtual_link
        + ", vnf_address="
        + vnf_address
        + ", status="
        + status
        + ", notification="
        + notification
        + ", runtime_policy_info="
        + runtime_policy_info
        + ", name='"
        + name
        + '\''
        + ", type='"
        + type
        + '\''
        + ", endpoint='"
        + endpoint
        + '\''
        + ", task='"
        + task
        + '\''
        + ", requires="
        + requires
        + ", provides="
        + provides
        + ", cyclicDependency="
        + cyclicDependency
        + ", packageId='"
        + packageId
        + '\''
        + '}';
  }
}
