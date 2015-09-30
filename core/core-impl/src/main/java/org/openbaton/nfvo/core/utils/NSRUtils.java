/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.core.utils;

import org.openbaton.catalogue.mano.common.AutoScalePolicy;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.record.*;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lto on 11/05/15.
 */
public class NSRUtils {


    private static Logger log = LoggerFactory.getLogger(NSRUtils.class);

    public static NetworkServiceRecord createNetworkServiceRecord(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();
        networkServiceRecord.setDescriptor_reference(networkServiceDescriptor.getId());
        networkServiceRecord.setName(networkServiceDescriptor.getName());
        networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
        networkServiceRecord.setMonitoring_parameter(new HashSet<String>());
        networkServiceRecord.getMonitoring_parameter().addAll(networkServiceDescriptor.getMonitoring_parameter());
        networkServiceRecord.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
        networkServiceRecord.getAuto_scale_policy().addAll(networkServiceDescriptor.getAuto_scale_policy());
        networkServiceRecord.setVnfr(new HashSet<VirtualNetworkFunctionRecord>());
        networkServiceRecord.setVnf_dependency(new HashSet<VNFRecordDependency>());

        networkServiceRecord.setLifecycle_event(new HashSet<LifecycleEvent>());
        Set<PhysicalNetworkFunctionRecord> pnfrs = new HashSet<PhysicalNetworkFunctionRecord>();
        if (networkServiceDescriptor.getPnfd() != null)
            for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor : networkServiceDescriptor.getPnfd()) {
                pnfrs.add(NSRUtils.createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
            }
        networkServiceRecord.setPnfr(pnfrs);
        networkServiceRecord.setStatus(Status.NULL);
        networkServiceRecord.setVnffgr(new HashSet<VNFForwardingGraphRecord>());
        networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
        networkServiceRecord.setVlr(new HashSet<VirtualLinkRecord>());
        if (networkServiceDescriptor.getVld() != null) {
            for (VirtualLinkDescriptor virtualLinkDescriptor : networkServiceDescriptor.getVld()) {
                VirtualLinkRecord vlr = NSRUtils.createVirtualLinkRecord(virtualLinkDescriptor);
                vlr.setParent_ns(networkServiceDescriptor.getId());
                networkServiceRecord.getVlr().add(vlr);
            }
        }
        return networkServiceRecord;
    }

    public static void setDependencies(NetworkServiceDescriptor networkServiceDescriptor, NetworkServiceRecord networkServiceRecord) throws BadFormatException {

        for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()) {
            boolean found = false;
            for (VNFRecordDependency vnfRecordDependency : networkServiceRecord.getVnf_dependency()) {
                if (vnfRecordDependency.getTarget().equals(vnfDependency.getTarget().getName())) { // if there is a vnfRecordDepenendency with the same target
                    // I find the source
                    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()) {
                        log.debug("Source is: " + vnfDependency.getSource().getName() + ". Target is: " + vnfDependency.getTarget().getName() + ". VNFR is: " + virtualNetworkFunctionDescriptor.getName());
                        if (vnfDependency.getSource().getName().equals(virtualNetworkFunctionDescriptor.getName())) {
                            vnfRecordDependency.getIdType().put(virtualNetworkFunctionDescriptor.getName(), virtualNetworkFunctionDescriptor.getType());
                            DependencyParameters dependencyParameters = vnfRecordDependency.getParameters().get(virtualNetworkFunctionDescriptor.getType());
                            //If there are no dependencyParameter of that type
                            if (dependencyParameters == null) {
                                dependencyParameters = new DependencyParameters();
                                dependencyParameters.setParameters(new HashMap<String, String>());
                            }
                            for (String key : vnfDependency.getParameters()) {
                                dependencyParameters.getParameters().put(key, "");
                            }
                            vnfRecordDependency.getParameters().put(virtualNetworkFunctionDescriptor.getType(), dependencyParameters);
                        }
                    }
                    found = true;
                }
            }

            if (!found) { // there is not yet a vnfrDepenency with this target, I add it
                VNFRecordDependency vnfRecordDependency = new VNFRecordDependency();
                vnfRecordDependency.setIdType(new HashMap<String, String>());
                vnfRecordDependency.setParameters(new HashMap<String, DependencyParameters>());
                for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()) {

                    if (vnfDependency.getSource().getName().equals(virtualNetworkFunctionDescriptor.getName())) {
                        vnfRecordDependency.getIdType().put(virtualNetworkFunctionDescriptor.getName(), virtualNetworkFunctionDescriptor.getType());
                        DependencyParameters dependencyParameters = new DependencyParameters();
                        dependencyParameters.setParameters(new HashMap<String, String>());
                        for (String key : vnfDependency.getParameters()) {
                            log.debug("Adding parameter to dependency: " + key);
                            dependencyParameters.getParameters().put(key, "");
                        }
                        vnfRecordDependency.getParameters().put(virtualNetworkFunctionDescriptor.getType(), dependencyParameters);
                    }

                    if (virtualNetworkFunctionDescriptor.getName().equals(vnfDependency.getTarget().getName()))
                        vnfRecordDependency.setTarget(virtualNetworkFunctionDescriptor.getName());
                }
                log.debug("Adding dependency " + vnfRecordDependency);
                networkServiceRecord.getVnf_dependency().add(vnfRecordDependency);
            }
        }
    }

    public static VirtualLinkRecord createVirtualLinkRecord(VirtualLinkDescriptor virtualLinkDescriptor) {
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

    public static PhysicalNetworkFunctionRecord createPhysicalNetworkFunctionRecord(PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor) {
        PhysicalNetworkFunctionRecord physicalNetworkFunctionRecord = new PhysicalNetworkFunctionRecord();
        // TODO implement it
        return physicalNetworkFunctionRecord;
    }

}
