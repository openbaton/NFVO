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

import java.util.*;
import org.openbaton.catalogue.mano.common.AutoScalePolicy;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.record.LinkStatus;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.PhysicalNetworkFunctionRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFForwardingGraphRecord;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 11/05/15. */
public class NSRUtils {

  private static final Logger log = LoggerFactory.getLogger(NSRUtils.class);

  public static NetworkServiceRecord createNetworkServiceRecord(
      NetworkServiceDescriptor networkServiceDescriptor) {
    NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();
    networkServiceRecord.setDescriptor_reference(networkServiceDescriptor.getId());
    networkServiceRecord.setName(networkServiceDescriptor.getName());
    networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
    networkServiceRecord.setMonitoring_parameter(new HashSet<String>());
    networkServiceRecord
        .getMonitoring_parameter()
        .addAll(networkServiceDescriptor.getMonitoring_parameter());
    networkServiceRecord.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
    networkServiceRecord
        .getAuto_scale_policy()
        .addAll(networkServiceDescriptor.getAuto_scale_policy());
    networkServiceRecord.setVnfr(new HashSet<VirtualNetworkFunctionRecord>());
    networkServiceRecord.setVnf_dependency(new HashSet<VNFRecordDependency>());

    networkServiceRecord.setLifecycle_event(new HashSet<LifecycleEvent>());
    Set<PhysicalNetworkFunctionRecord> pnfrs = new HashSet<>();
    if (networkServiceDescriptor.getPnfd() != null)
      for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor :
          networkServiceDescriptor.getPnfd()) {
        pnfrs.add(createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
      }
    networkServiceRecord.setPnfr(pnfrs);
    networkServiceRecord.setStatus(Status.NULL);
    networkServiceRecord.setVnffgr(new HashSet<VNFForwardingGraphRecord>());
    networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
    networkServiceRecord.setVlr(new HashSet<VirtualLinkRecord>());
    if (networkServiceDescriptor.getVld() != null) {
      for (VirtualLinkDescriptor virtualLinkDescriptor : networkServiceDescriptor.getVld()) {
        VirtualLinkRecord vlr = createVirtualLinkRecord(virtualLinkDescriptor);
        vlr.setParent_ns(networkServiceDescriptor.getId());
        networkServiceRecord.getVlr().add(vlr);
      }
    }
    // Config. VNFFG
    if (networkServiceDescriptor.getVnffgd().size() != 0) {
      for (VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor :
          networkServiceDescriptor.getVnffgd()) {
        VNFForwardingGraphRecord vnffgr =
            NSRUtils.createVNFForwardingGraphRecord(vnfForwardingGraphDescriptor);
        networkServiceRecord.getVnffgr().add(vnffgr);
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
                    .getTarget()
                    .getName())) { // if there is a vnfRecordDepenendency with the same target
          // I find the source
          for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
              networkServiceDescriptor.getVnfd()) {
            log.debug(
                "Source is: "
                    + vnfDependency.getSource().getName()
                    + ". Target is: "
                    + vnfDependency.getTarget().getName()
                    + ". VNFR is: "
                    + virtualNetworkFunctionDescriptor.getName());
            if (vnfDependency
                .getSource()
                .getName()
                .equals(virtualNetworkFunctionDescriptor.getName())) {
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
                dependencyParameters.setParameters(new HashMap<String, String>());
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
        vnfRecordDependency.setIdType(new HashMap<String, String>());
        vnfRecordDependency.setParameters(new HashMap<String, DependencyParameters>());
        for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
            networkServiceDescriptor.getVnfd()) {

          if (vnfDependency
              .getSource()
              .getName()
              .equals(virtualNetworkFunctionDescriptor.getName())) {
            vnfRecordDependency
                .getIdType()
                .put(
                    virtualNetworkFunctionDescriptor.getName(),
                    virtualNetworkFunctionDescriptor.getType());
            DependencyParameters dependencyParameters = new DependencyParameters();
            dependencyParameters.setParameters(new HashMap<String, String>());
            for (String key : vnfDependency.getParameters()) {
              log.debug("Adding parameter to dependency: " + key);
              dependencyParameters.getParameters().put(key, "");
            }
            vnfRecordDependency
                .getParameters()
                .put(virtualNetworkFunctionDescriptor.getType(), dependencyParameters);
          }

          if (virtualNetworkFunctionDescriptor
              .getName()
              .equals(vnfDependency.getTarget().getName()))
            vnfRecordDependency.setTarget(virtualNetworkFunctionDescriptor.getName());

          vnfRecordDependency.setVnfcParameters(new HashMap<String, VNFCDependencyParameters>());
          VNFCDependencyParameters vnfcDependencyParameters = new VNFCDependencyParameters();
          vnfcDependencyParameters.setParameters(new HashMap<String, DependencyParameters>());

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
    virtualLinkRecord.setExtId(null);
    virtualLinkRecord.setVim_id(null);

    virtualLinkRecord.setAllocated_capacity(new HashSet<String>());
    virtualLinkRecord.setAudit_log(new HashSet<String>());
    virtualLinkRecord.setNotification(new HashSet<String>());
    virtualLinkRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
    virtualLinkRecord.setVnffgr_reference(new HashSet<VNFForwardingGraphRecord>());
    virtualLinkRecord.setConnection(new HashSet<String>());

    //TODO think about test_access -> different types on VLD and VLR
    //virtualLinkRecord.setTest_access("");

    virtualLinkRecord.setQos(new HashSet<String>());
    for (String qos : virtualLinkDescriptor.getQos()) {
      virtualLinkRecord.getQos().add(qos);
    }

    return virtualLinkRecord;
  }

  public static VNFForwardingGraphRecord createVNFForwardingGraphRecord(
      VNFForwardingGraphDescriptor vnffgDescriptor) {

    VNFForwardingGraphRecord vnfforwardingGraphRecord = new VNFForwardingGraphRecord();

    vnfforwardingGraphRecord.setDescriptor_reference(vnffgDescriptor);

    //  vnfforwardingGraphRecord.setParent_ns(new NetworkServiceRecord());

    vnfforwardingGraphRecord.setDependent_virtual_link(new HashSet<VirtualLinkRecord>());

    /*  for(VirtualLinkRecord vlr: networkServiceRecord.getVlr()){
        for(VirtualLinkDescriptor vld: vnffgDescriptor.getDependent_virtual_link()){
            if(vlr.getName().equals(vld.getName())){
                vnfforwardingGraphRecord.getDependent_virtual_link().add(vlr);
                break;

            }
        }
    }
     */
    // I am not sure how the status is managed !!
    vnfforwardingGraphRecord.setStatus(Status.ACTIVE);

    // I am not sure also how the Notification and LifeCycle Event can be done
    vnfforwardingGraphRecord.setNotification(new HashSet<String>());
    vnfforwardingGraphRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
    vnfforwardingGraphRecord.setAudit_log(" ");

    vnfforwardingGraphRecord.setNetwork_forwarding_path(new HashSet<NetworkForwardingPath>());

    for (NetworkForwardingPath networkForwardingPath :
        vnffgDescriptor.getNetwork_forwarding_path()) {
      NetworkForwardingPath newNetworkForwardingPath = new NetworkForwardingPath();
      Policy newPolicy = new Policy();
      ACLMatchingCriteria newACL = new ACLMatchingCriteria();
      newACL.setDestinationIP(
          networkForwardingPath.getPolicy().getMatchingCriteria().getDestinationIP());
      newACL.setSourceIP(networkForwardingPath.getPolicy().getMatchingCriteria().getSourceIP());
      newACL.setProtocol(networkForwardingPath.getPolicy().getMatchingCriteria().getProtocol());
      newACL.setDestinationPort(
          networkForwardingPath.getPolicy().getMatchingCriteria().getDestinationPort());
      newACL.setSourcePort(networkForwardingPath.getPolicy().getMatchingCriteria().getSourcePort());
      newPolicy.setMatchingCriteria(newACL);
      //newPolicy.setQoSLevel(networkForwardingPath.getPolicy().getQoSLevel());
      newNetworkForwardingPath.setPolicy(newPolicy);

      /*List<Connection> connections

      for (Connection entry : networkForwardingPath.getConnections()) {
          newConnection.put(entry.getKey(), entry.getValue());
      }*/
      newNetworkForwardingPath.setConnections(networkForwardingPath.getConnections());

      vnfforwardingGraphRecord.getNetwork_forwarding_path().add(newNetworkForwardingPath);
    }
    vnfforwardingGraphRecord.setConnection_point(new HashSet<VNFDConnectionPoint>());
    vnfforwardingGraphRecord.setMember_vnfs(new HashSet<VirtualNetworkFunctionRecord>());

    // The consititute VNFRs need to be add to the VNFFGR but at the time of Creation of VNFFGR, the VNFRs info is not ready, So it needs to be handled.
    /*  if(networkServiceRecord.getVnfr().size()!=0){
      for(VirtualNetworkFunctionRecord vnfr:networkServiceRecord.getVnfr()) {
         for (CostituentVNF costituentVNF : vnffgDescriptor.getConstituent_vnfs()) {

            if(costituentVNF.getVnf_reference().equals(vnfr.getDescriptor_reference())){
                 vnfforwardingGraphRecord.getMember_vnfs().add(vnfr);
                 break;
             }
         }

     }
    }

     */
    vnfforwardingGraphRecord.setSymmetricity(vnffgDescriptor.isSymmetrical());
    vnfforwardingGraphRecord.setVendor(vnffgDescriptor.getVendor());
    vnfforwardingGraphRecord.setVersion(vnffgDescriptor.getVersion());
    vnfforwardingGraphRecord.setNumber_of_endpoints(vnffgDescriptor.getNumber_of_endpoints());
    vnfforwardingGraphRecord.setNumber_of_virtual_links(
        vnfforwardingGraphRecord.getDependent_virtual_link().size());

    // Need to be adjusted if the PNFS are supported
    // vnfforwardingGraphRecord.setNumber_of_vnfs(vnfforwardingGraphRecord.getMember_vnfs().size());
    // vnfforwardingGraphRecord.setNumber_of_pnfs(0);

    return vnfforwardingGraphRecord;
  }

  private static PhysicalNetworkFunctionRecord createPhysicalNetworkFunctionRecord(
      PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor) {
    // TODO implement it
    return new PhysicalNetworkFunctionRecord();
  }
}
