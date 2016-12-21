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

package org.openbaton.catalogue.mano.descriptor;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.openbaton.catalogue.mano.common.NFVEntityDescriptor;
import org.openbaton.catalogue.mano.common.Security;

/**
 * Created by lto on 05/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkServiceDescriptor extends NFVEntityDescriptor {

  /**
   * VNF which is part of the Network Service, see clause 6.3.1. This element is required, for
   * example, when the Network Service is being built top-down or instantiating the member VNFs as
   * well.
   */
  @ManyToMany(
    cascade = {
      CascadeType.MERGE,
      CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.DETACH /*CascadeType.REMOVE*/
    },
    fetch = FetchType.EAGER
  )
  private Set<VirtualNetworkFunctionDescriptor> vnfd;
  /**
   * Describe dependencies between VNF. Defined in terms of source and target VNF i.e. target VNF
   * "depends on" source VNF. In other words a source VNF shall exist and connect to the service
   * before target VNF can be initiated/deployed and connected. This element would be used, for
   * example, to define the sequence in which various numbered network nodes and links within a VNF
   * FG should be instantiated by the NFV Orchestrator.
   */
  @OneToMany(
    cascade = {CascadeType.ALL /*CascadeType.REFRESH*/},
    fetch = FetchType.EAGER
  )
  private Set<VNFDependency> vnf_dependency;
  /* See PhysicalNetworkFunctionDescriptor class for description */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<PhysicalNetworkFunctionDescriptor> pnfd;
  /*
   * This is a signature of nsd to prevent tampering. The particular hash
   * algorithm used to compute the signature, together with the corresponding
   * cryptographic certificate to validate the signature should also be
   * included. Not mandatory from NFV. TODO could also be called Security and
   * used for all the objects that need it.
   */
  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Security nsd_security;

  private boolean enabled;

  public NetworkServiceDescriptor() {}

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Set<VirtualNetworkFunctionDescriptor> getVnfd() {
    if (vnfd == null) vnfd = new HashSet<>();
    return vnfd;
  }

  public void setVnfd(Set<VirtualNetworkFunctionDescriptor> vnfd) {
    this.vnfd = vnfd;
  }

  public Set<VNFDependency> getVnf_dependency() {
    if (vnf_dependency == null) vnf_dependency = new HashSet<>();
    return vnf_dependency;
  }

  public void setVnf_dependency(Set<VNFDependency> vnf_dependency) {
    this.vnf_dependency = vnf_dependency;
  }

  public Set<PhysicalNetworkFunctionDescriptor> getPnfd() {
    if (pnfd == null) pnfd = new HashSet<>();
    return pnfd;
  }

  public void setPnfd(Set<PhysicalNetworkFunctionDescriptor> pnfd) {
    this.pnfd = pnfd;
  }

  public Security getNsd_security() {
    return nsd_security;
  }

  public void setNsd_security(Security nsd_security) {
    this.nsd_security = nsd_security;
  }

  @Override
  public String toString() {
    return "NetworkServiceDescriptor [vnfd="
        + vnfd
        + ", vnf_dependency="
        + vnf_dependency
        + ", pnfd="
        + pnfd
        + ", nsd_security="
        + nsd_security
        + ", id="
        + id
        + ", hb_version="
        + hb_version
        + ", vendor="
        + vendor
        + ", version="
        + version
        + ", vnffgd="
        + vnffgd
        + ", vld="
        + vld
        + ", lifecycle_event="
        + monitoring_parameter
        + ", service_deployment_flavour="
        + service_deployment_flavour
        + ", auto_scale_policy="
        + auto_scale_policy
        + ", connection_point="
        + connection_point
        + "]";
  }
}
