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

import java.io.Serializable;
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
import org.openbaton.catalogue.mano.common.AutoScalePolicy;
import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.common.NetworkServiceDeploymentFlavour;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementPolicy;
import org.openbaton.catalogue.util.IdGenerator;

/**
 * Created by lto on 06/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkServiceRecord implements Serializable {
  @Id private String id;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<AutoScalePolicy> auto_scale_policy;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<ConnectionPoint> connection_point;
  /** Monitoring parameter used in this instance. */
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> monitoring_parameter;
  /**
   * References the nsd:service_deployment_flavour used to instantiate this Network Service
   * instance.
   */
  @OneToOne(cascade = CascadeType.REFRESH)
  private NetworkServiceDeploymentFlavour service_deployment_flavour;

  private String vendor;
  private String projectId;
  private String task;

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  private String version;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<VirtualLinkRecord> vlr;

  @OneToMany(
    cascade = {
      /*CascadeType.PERSIST, CascadeType.MERGE*/
      CascadeType.ALL
    },
    fetch = FetchType.EAGER
  )
  private Set<VirtualNetworkFunctionRecord> vnfr;

  @OneToMany(
    cascade = {
      /*CascadeType.PERSIST, CascadeType.MERGE*/
      CascadeType.ALL
    },
    fetch = FetchType.EAGER
  )
  private Set<VNFRecordDependency> vnf_dependency;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<LifecycleEvent> lifecycle_event;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<VNFForwardingGraphRecord> vnffgr;
  /** At least one */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<PhysicalNetworkFunctionRecord> pnfr;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<FaultManagementPolicy> faultManagementPolicy;

  /** The reference to the Network Service Descriptor used to instantiate this Network Service. */
  private String descriptor_reference;
  /**
   * Resource reservation information identification (potentially per individual VIM) for NFVI
   * resources reserved for this NS instance. TODO: find an appropriate type for this field
   */
  private String resource_reservation;
  /**
   * Generic placeholder for input information related to NS orchestration and management policies
   * to be applied during runtime of a specific NS instance (e.g. for NS prioritization, etc.).
   * TODO: find an appropriate type for this field
   */
  private String runtime_policy_info;
  /**
   * Flag to report status of the Network Service.
   *
   * <p>Implementation thoughts: the states are defined in
   * http://www.etsi.org/deliver/etsi_gs/NFV-SWA/001_099/001/01.01.01_60/gs_NFV-SWA001v010101p.pdf
   * so for what concerns the NSR, the state are:
   *
   * <p>* Instantiated Configured - Inactive) A NSR Instance is created not ready for service. *
   * Instantiated Configured - Active) A NSR Instance is ready to serve. * Terminated) A NSR has
   * ceased to exist.
   *
   * <p>the Null doesn't exist since when the NSR is created will be already in configuration
   * process.
   */
  @Enumerated(EnumType.STRING)
  private Status status;
  /** System that has registered to received notifications of status changes */
  private String notification;
  /** Record of significant Network Service lifecycle events. */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<LifecycleEvent> lifecycle_event_history;
  /** Record of detailed operational events. TODO: maybe a pointer to a file? */
  private String audit_log;

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  private String createdAt;

  public Set<String> getKeyNames() {
    return keyNames;
  }

  public void setKeyNames(Set<String> keyNames) {
    this.keyNames = keyNames;
  }

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> keyNames;

  private String name;

  public NetworkServiceRecord() {}

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

  public Set<AutoScalePolicy> getAuto_scale_policy() {
    return auto_scale_policy;
  }

  public void setAuto_scale_policy(Set<AutoScalePolicy> auto_scale_policy) {
    this.auto_scale_policy = auto_scale_policy;
  }

  public Set<ConnectionPoint> getConnection_point() {
    return connection_point;
  }

  public void setConnection_point(Set<ConnectionPoint> connection_point) {
    this.connection_point = connection_point;
  }

  public Set<String> getMonitoring_parameter() {
    return monitoring_parameter;
  }

  public void setMonitoring_parameter(Set<String> monitoring_parameter) {
    this.monitoring_parameter = monitoring_parameter;
  }

  public NetworkServiceDeploymentFlavour getService_deployment_flavour() {
    return service_deployment_flavour;
  }

  public void setService_deployment_flavour(
      NetworkServiceDeploymentFlavour service_deployment_flavour) {
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

  public Set<VirtualLinkRecord> getVlr() {
    return vlr;
  }

  public void setVlr(Set<VirtualLinkRecord> vlr) {
    this.vlr = vlr;
  }

  public Set<VirtualNetworkFunctionRecord> getVnfr() {
    return vnfr;
  }

  public void setVnfr(Set<VirtualNetworkFunctionRecord> vnfr) {
    this.vnfr = vnfr;
  }

  public Set<LifecycleEvent> getLifecycle_event() {
    return lifecycle_event;
  }

  public void setLifecycle_event(Set<LifecycleEvent> lifecycle_event) {
    this.lifecycle_event = lifecycle_event;
  }

  public Set<VNFRecordDependency> getVnf_dependency() {
    return vnf_dependency;
  }

  public void setVnf_dependency(Set<VNFRecordDependency> vnf_dependency) {
    this.vnf_dependency = vnf_dependency;
  }

  public Set<VNFForwardingGraphRecord> getVnffgr() {
    return vnffgr;
  }

  public void setVnffgr(Set<VNFForwardingGraphRecord> vnffgr) {
    this.vnffgr = vnffgr;
  }

  public Set<PhysicalNetworkFunctionRecord> getPnfr() {
    return pnfr;
  }

  public void setPnfr(Set<PhysicalNetworkFunctionRecord> pnfr) {
    this.pnfr = pnfr;
  }

  public String getDescriptor_reference() {
    return descriptor_reference;
  }

  public void setDescriptor_reference(String descriptor_reference) {
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<FaultManagementPolicy> getFaultManagementPolicy() {
    return faultManagementPolicy;
  }

  public void setFaultManagementPolicy(Set<FaultManagementPolicy> faultManagementPolicy) {
    this.faultManagementPolicy = faultManagementPolicy;
  }

  @Override
  public String toString() {
    return "NetworkServiceRecord{"
        + "id='"
        + id
        + '\''
        + ", auto_scale_policy="
        + auto_scale_policy
        + ", connection_point="
        + connection_point
        + ", monitoring_parameter="
        + monitoring_parameter
        + ", service_deployment_flavour="
        + service_deployment_flavour
        + ", vendor='"
        + vendor
        + '\''
        + ", projectId='"
        + projectId
        + '\''
        + ", task='"
        + task
        + '\''
        + ", version='"
        + version
        + '\''
        + ", vlr="
        + vlr
        + ", vnfr="
        + vnfr
        + ", vnf_dependency="
        + vnf_dependency
        + ", lifecycle_event="
        + lifecycle_event
        + ", vnffgr="
        + vnffgr
        + ", pnfr="
        + pnfr
        + ", faultManagementPolicy="
        + faultManagementPolicy
        + ", descriptor_reference='"
        + descriptor_reference
        + '\''
        + ", resource_reservation='"
        + resource_reservation
        + '\''
        + ", runtime_policy_info='"
        + runtime_policy_info
        + '\''
        + ", status="
        + status
        + ", notification='"
        + notification
        + '\''
        + ", lifecycle_event_history="
        + lifecycle_event_history
        + ", audit_log='"
        + audit_log
        + '\''
        + ", createdAt='"
        + createdAt
        + '\''
        + ", keyNames="
        + keyNames
        + ", name='"
        + name
        + '\''
        + '}';
  }

  public void setTask(String task) {
    this.task = task;
  }

  public String getTask() {
    return task;
  }
}
