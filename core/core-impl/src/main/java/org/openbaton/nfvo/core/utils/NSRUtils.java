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

package org.openbaton.nfvo.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.LinkStatus;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.PhysicalNetworkFunctionRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NSRUtils {

  private static final Logger log = LoggerFactory.getLogger(NSRUtils.class);

  public static SimpleDateFormat getFormat() {
    return new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
  }

  public static NetworkServiceRecord createNetworkServiceRecord(
      NetworkServiceDescriptor networkServiceDescriptor) {

    NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();
    networkServiceRecord.setCreatedAt(getFormat().format(new Date()));
    networkServiceRecord.setUpdatedAt(getFormat().format(new Date()));
    networkServiceRecord.setTask("Onboarding");
    networkServiceRecord.setKeyNames(new HashSet<>());
    networkServiceRecord.setDescriptor_reference(networkServiceDescriptor.getId());
    networkServiceRecord.setName(networkServiceDescriptor.getName());
    networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
    networkServiceRecord.setMonitoring_parameter(new HashSet<>());
    networkServiceRecord
        .getMonitoring_parameter()
        .addAll(networkServiceDescriptor.getMonitoring_parameter());
    networkServiceRecord.setAuto_scale_policy(new HashSet<>());
    networkServiceRecord
        .getAuto_scale_policy()
        .addAll(networkServiceDescriptor.getAuto_scale_policy());
    networkServiceRecord.setVnfr(new HashSet<>());
    networkServiceRecord.setVnf_dependency(new HashSet<>());

    networkServiceRecord.setLifecycle_event(new HashSet<>());
    Set<PhysicalNetworkFunctionRecord> pnfrs = new HashSet<>();
    if (networkServiceDescriptor.getPnfd() != null)
      for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor :
          networkServiceDescriptor.getPnfd()) {
        pnfrs.add(createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
      }
    networkServiceRecord.setPnfr(pnfrs);
    networkServiceRecord.setStatus(Status.NULL);
    networkServiceRecord.setVnffgr(new HashSet<>());
    networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
    networkServiceRecord.setVlr(new HashSet<>());
    if (networkServiceDescriptor.getVld() != null) {
      for (VirtualLinkDescriptor virtualLinkDescriptor : networkServiceDescriptor.getVld()) {
        VirtualLinkRecord vlr = createVirtualLinkRecord(virtualLinkDescriptor);
        vlr.setParent_ns(networkServiceDescriptor.getId());
        networkServiceRecord.getVlr().add(vlr);
      }
    }
    return networkServiceRecord;
  }

  public static void setDependencies(
      NetworkServiceDescriptor networkServiceDescriptor,
      NetworkServiceRecord networkServiceRecord) {

    for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()) {
      boolean found = false;
      for (VNFRecordDependency vnfRecordDependency : networkServiceRecord.getVnf_dependency()) {
        if (vnfRecordDependency
            .getTarget()
            .equals(
                vnfDependency
                    .getTarget())) { // if there is a vnfRecordDepenendency with the same target
          // I find the source
          for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
              networkServiceDescriptor.getVnfd()) {
            log.debug(
                "Source is: "
                    + vnfDependency.getSource()
                    + ". Target is: "
                    + vnfDependency.getTarget()
                    + ". VNFR is: "
                    + virtualNetworkFunctionDescriptor.getName());
            if (vnfDependency.getSource().equals(virtualNetworkFunctionDescriptor.getName())) {
              vnfRecordDependency
                  .getIdType()
                  .put(
                      virtualNetworkFunctionDescriptor.getName(),
                      virtualNetworkFunctionDescriptor.getType());
              DependencyParameters dependencyParameters =
                  vnfRecordDependency
                      .getParameters()
                      .get(virtualNetworkFunctionDescriptor.getType());
              //If there are no dependencyParameter of that type
              if (dependencyParameters == null) {
                dependencyParameters = new DependencyParameters();
                dependencyParameters.setParameters(new HashMap<>());
              }
              for (String key : vnfDependency.getParameters()) {
                dependencyParameters.getParameters().put(key, "");
              }
              vnfRecordDependency
                  .getParameters()
                  .put(virtualNetworkFunctionDescriptor.getType(), dependencyParameters);
            }
          }
          found = true;
        }
      }

      if (!found) { // there is not yet a vnfrDepenency with this target, I add it
        VNFRecordDependency vnfRecordDependency = new VNFRecordDependency();
        vnfRecordDependency.setIdType(new HashMap<>());
        vnfRecordDependency.setParameters(new HashMap<>());
        for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
            networkServiceDescriptor.getVnfd()) {

          if (vnfDependency.getSource().equals(virtualNetworkFunctionDescriptor.getName())) {
            vnfRecordDependency
                .getIdType()
                .put(
                    virtualNetworkFunctionDescriptor.getName(),
                    virtualNetworkFunctionDescriptor.getType());
            DependencyParameters dependencyParameters = new DependencyParameters();
            dependencyParameters.setParameters(new HashMap<>());
            for (String key : vnfDependency.getParameters()) {
              log.debug("Adding parameter to dependency: " + key);
              dependencyParameters.getParameters().put(key, "");
            }
            vnfRecordDependency
                .getParameters()
                .put(virtualNetworkFunctionDescriptor.getType(), dependencyParameters);
          }

          if (virtualNetworkFunctionDescriptor.getName().equals(vnfDependency.getTarget()))
            vnfRecordDependency.setTarget(virtualNetworkFunctionDescriptor.getName());

          vnfRecordDependency.setVnfcParameters(new HashMap<>());
          VNFCDependencyParameters vnfcDependencyParameters = new VNFCDependencyParameters();
          vnfcDependencyParameters.setParameters(new HashMap<>());

          vnfRecordDependency
              .getVnfcParameters()
              .put(virtualNetworkFunctionDescriptor.getType(), vnfcDependencyParameters);
        }
        log.debug("Adding dependency " + vnfRecordDependency);
        networkServiceRecord.getVnf_dependency().add(vnfRecordDependency);
      }
    }
  }

  private static VirtualLinkRecord createVirtualLinkRecord(
      VirtualLinkDescriptor virtualLinkDescriptor) {
    VirtualLinkRecord virtualLinkRecord = new VirtualLinkRecord();
    virtualLinkRecord.setName(virtualLinkDescriptor.getName());
    virtualLinkRecord.setConnectivity_type(virtualLinkDescriptor.getConnectivity_type());
    virtualLinkRecord.setDescriptor_reference(virtualLinkDescriptor.getId());
    virtualLinkRecord.setRoot_requirement(virtualLinkDescriptor.getRoot_requirement());
    virtualLinkRecord.setLeaf_requirement(virtualLinkDescriptor.getLeaf_requirement());
    virtualLinkRecord.setVendor(virtualLinkDescriptor.getVendor());

    virtualLinkRecord.setStatus(LinkStatus.LINKDOWN);

    virtualLinkRecord.setParent_ns(null);
    virtualLinkRecord.setExtId(virtualLinkDescriptor.getExtId());
    virtualLinkRecord.setVim_id(null);

    virtualLinkRecord.setAllocated_capacity(new HashSet<>());
    virtualLinkRecord.setAudit_log(new HashSet<>());
    virtualLinkRecord.setNotification(new HashSet<>());
    virtualLinkRecord.setLifecycle_event_history(new HashSet<>());
    virtualLinkRecord.setVnffgr_reference(new HashSet<>());
    virtualLinkRecord.setConnection(new HashSet<>());

    //TODO think about test_access -> different types on VLD and VLR
    //virtualLinkRecord.setTest_access("");

    virtualLinkRecord.setQos(new HashSet<>());
    for (String qos : virtualLinkDescriptor.getQos()) {
      virtualLinkRecord.getQos().add(qos);
    }

    return virtualLinkRecord;
  }

  private static PhysicalNetworkFunctionRecord createPhysicalNetworkFunctionRecord(
      PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor) {
    // TODO implement it
    return new PhysicalNetworkFunctionRecord();
  }
}
