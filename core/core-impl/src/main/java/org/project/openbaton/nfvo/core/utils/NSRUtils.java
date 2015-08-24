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

package org.project.openbaton.nfvo.core.utils;

import org.project.openbaton.catalogue.mano.common.AutoScalePolicy;
import org.project.openbaton.catalogue.mano.common.ConnectionPoint;
import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.*;
import org.project.openbaton.catalogue.mano.record.*;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

/**
 * Created by lto on 11/05/15.
 */
public class NSRUtils {

    @Autowired
    @Qualifier("VNFDRepository")
    private static GenericRepository<VirtualNetworkFunctionDescriptor> vnfdRepository;

    private static Logger log = LoggerFactory.getLogger(NSRUtils.class);

    public static NetworkServiceRecord createNetworkServiceRecord(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        log.debug("" + networkServiceDescriptor);
        NetworkServiceRecord networkServiceRecord = new NetworkServiceRecord();
        networkServiceRecord.setDescriptor_reference(networkServiceDescriptor.getId());
        networkServiceRecord.setName(networkServiceDescriptor.getName());
        networkServiceRecord.setVendor(networkServiceDescriptor.getVendor());
        networkServiceRecord.setMonitoring_parameter(new HashSet<String>());
        networkServiceRecord.getMonitoring_parameter().addAll(networkServiceDescriptor.getMonitoring_parameter());
        networkServiceRecord.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
        networkServiceRecord.getAuto_scale_policy().addAll(networkServiceDescriptor.getAuto_scale_policy());
        networkServiceRecord.setVnfr(new HashSet<VirtualNetworkFunctionRecord>());
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = NSRUtils.createVirtualNetworkFunctionRecord(vnfd, networkServiceRecord.getId());
            networkServiceRecord.getVnfr().add(virtualNetworkFunctionRecord);
        }
        //TODO set dependencies!!! (DONE)
        networkServiceRecord.setVnf_dependency(new HashSet<VNFRecordDependency>());
//        setDependencies(networkServiceDescriptor, networkServiceRecord);

        networkServiceRecord.setLifecycle_event(new HashSet<LifecycleEvent>());
//        networkServiceRecord.getLifecycle_event().addAll(networkServiceDescriptor.getLifecycle_event());
        Set<PhysicalNetworkFunctionRecord> pnfrs = new HashSet<PhysicalNetworkFunctionRecord>();
        if (networkServiceDescriptor.getPnfd() != null)
            for (PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor : networkServiceDescriptor.getPnfd()) {
                pnfrs.add(NSRUtils.createPhysicalNetworkFunctionRecord(physicalNetworkFunctionDescriptor));
            }
        networkServiceRecord.setPnfr(pnfrs);
        networkServiceRecord.setStatus(Status.NULL);
        networkServiceRecord.setVnffgr(new HashSet<VNFForwardingGraphRecord>());
//      TODO translate them from descriptors to records
//        networkServiceRecord.getVnffgr().addAll(networkServiceDescriptor.getVnffgd());
        networkServiceRecord.setVersion(networkServiceDescriptor.getVersion());
        networkServiceRecord.setVlr(new HashSet<VirtualLinkRecord>());
        if (networkServiceDescriptor.getVld() != null) {
            for (VirtualLinkDescriptor virtualLinkDescriptor : networkServiceDescriptor.getVld()) {
                networkServiceRecord.getVlr().add(NSRUtils.createVirtualLinkRecord(virtualLinkDescriptor));
            }
        }
        return networkServiceRecord;
    }

    public static void setDependencies(NetworkServiceDescriptor networkServiceDescriptor, NetworkServiceRecord networkServiceRecord) throws BadFormatException {

        for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()) {

            boolean found = false;
            for (VNFRecordDependency vnfRecordDependency : networkServiceRecord.getVnf_dependency()) {

                if (vnfRecordDependency.getTarget().getName().equals(vnfDependency.getTarget().getName())) { // if there is a vnfRecordDepenendency with the same target

                    // I find the source
                    for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {

                        log.debug("Source is: " + vnfDependency.getSource().getName() + ". Target is: " + vnfDependency.getTarget().getName() + ". VNFR is: " + virtualNetworkFunctionRecord.getName());

                        if (vnfDependency.getSource().getName().equals(virtualNetworkFunctionRecord.getName())) {
                            vnfRecordDependency.getIdType().put(virtualNetworkFunctionRecord.getId(), virtualNetworkFunctionRecord.getType());

                            DependencyParameters dependencyParameters=vnfRecordDependency.getParameters().get(virtualNetworkFunctionRecord.getType());
                            //If there are no dependencyParameter of that type
                            if(dependencyParameters==null) {
                                dependencyParameters = new DependencyParameters();
                                dependencyParameters.setParameters(new HashMap<String, String>());
                            }
                            for (String key:vnfDependency.getParameters()) {
                                dependencyParameters.getParameters().put(key, "");
                            }
                            vnfRecordDependency.getParameters().put(virtualNetworkFunctionRecord.getType(), dependencyParameters);
                        }
                    }

                    found = true;

                }

            }
            if (!found) { // there is not yet a vnfrDepenency with this target, I add it
                VNFRecordDependency vnfRecordDependency = new VNFRecordDependency();
                vnfRecordDependency.setIdType(new HashMap<String, String>());
                vnfRecordDependency.setParameters(new HashMap<String, DependencyParameters>());
                for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {

                    if (vnfDependency.getSource().getName().equals(virtualNetworkFunctionRecord.getName())) {
                        vnfRecordDependency.getIdType().put(virtualNetworkFunctionRecord.getId(), virtualNetworkFunctionRecord.getType());
                        DependencyParameters dependencyParameters = new DependencyParameters();
                        dependencyParameters.setParameters(new HashMap<String, String>());
                        for (String key:vnfDependency.getParameters()) {
                            log.debug("Adding parameter to dependency: " + key);
                            dependencyParameters.getParameters().put(key,"");
                        }
                        vnfRecordDependency.getParameters().put(virtualNetworkFunctionRecord.getType(), dependencyParameters);
                    }

                    if (virtualNetworkFunctionRecord.getName().equals(vnfDependency.getTarget().getName()))
                        vnfRecordDependency.setTarget(virtualNetworkFunctionRecord);
                }
                log.debug("Adding dependency " + vnfRecordDependency);
                networkServiceRecord.getVnf_dependency().add(vnfRecordDependency);
            }
        }

        /*log.debug("New dependency are:");
        for (VNFRecordDependency vnfRecordDependency : networkServiceRecord.getVnf_dependency()) {
            log.debug("Target: " + vnfRecordDependency.getTarget().getName());
            for(Map.Entry<String, DependencyParameters> entry : vnfRecordDependency.getParameters().entrySet()){
                log.debug("Vnfr type: " + entry.getKey());
                for(Map.Entry<String, String> entryDependency : entry.getValue().getParameters().entrySet()){
                    log.debug("Parameters (key=value) "+entryDependency.toString());
                }
            }
        }*/

    }

    public static VirtualLinkRecord createVirtualLinkRecord(VirtualLinkDescriptor virtualLinkDescriptor) {
        VirtualLinkRecord virtualLinkRecord = new VirtualLinkRecord();
        // TODO implement it
        return virtualLinkRecord;
    }

    public static PhysicalNetworkFunctionRecord createPhysicalNetworkFunctionRecord(PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor) {
        PhysicalNetworkFunctionRecord physicalNetworkFunctionRecord = new PhysicalNetworkFunctionRecord();
        // TODO implement it
        return physicalNetworkFunctionRecord;
    }

    public static VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor vnfd, String nsr_id) throws NotFoundException, BadFormatException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
        virtualNetworkFunctionRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
        virtualNetworkFunctionRecord.setParent_ns_id(nsr_id);
        virtualNetworkFunctionRecord.setName(vnfd.getName());
        virtualNetworkFunctionRecord.setType(vnfd.getType());
        virtualNetworkFunctionRecord.setCyclicDependency(vnfd.hasCyclicDependency());

        Configuration requires = new Configuration();
        requires.setName("requires");
        requires.setConfigurationParameters(new HashSet<ConfigurationParameter>());
        virtualNetworkFunctionRecord.setRequires(requires);

        if (vnfd.getRequires() != null) {
            for (String key : vnfd.getRequires()) {
                ConfigurationParameter configurationParameter = new ConfigurationParameter();
                log.debug("Adding " + key + " to requires");
                configurationParameter.setConfKey(key);
                virtualNetworkFunctionRecord.getRequires().getConfigurationParameters().add(configurationParameter);
            }
        }

        Configuration provides = new Configuration();
        provides.setConfigurationParameters(new HashSet<ConfigurationParameter>());
        provides.setName("provides");
        virtualNetworkFunctionRecord.setProvides(provides);

        if (vnfd.getProvides() != null) {
            for (String key : vnfd.getProvides()) {
                ConfigurationParameter configurationParameter = new ConfigurationParameter();
                log.debug("Adding " + key + " to provides");
                configurationParameter.setConfKey(key);
                virtualNetworkFunctionRecord.getProvides().getConfigurationParameters().add(configurationParameter);
            }
        }

        if (vnfd.getVnfPackage() != null) {
            VNFPackage vnfPackage = new VNFPackage();
            vnfPackage.setImageLink(vnfd.getVnfPackage().getImageLink());
            vnfPackage.setScriptsLink(vnfd.getVnfPackage().getScriptsLink());
            vnfPackage.setName(vnfd.getVnfPackage().getName());

            //TODO check for ordering
            vnfPackage.setScripts(new HashSet<Script>());

            for (Script script : vnfd.getVnfPackage().getScripts()) {
                Script s = new Script();
                s.setName(script.getName());
                s.setPayload(script.getPayload());
                vnfPackage.getScripts().add(s);
            }

            vnfPackage.setImage(vnfd.getVnfPackage().getImage());
            vnfPackage.setExtId(vnfd.getVnfPackage().getExtId());
            virtualNetworkFunctionRecord.setVnfPackage(vnfPackage);
        }
        if (vnfd.getEndpoint() != null) {
            virtualNetworkFunctionRecord.setEndpoint(vnfd.getEndpoint());
        }
        else
            virtualNetworkFunctionRecord.setEndpoint(vnfd.getType());
        virtualNetworkFunctionRecord.setMonitoring_parameter(new HashSet<String>());
        virtualNetworkFunctionRecord.getMonitoring_parameter().addAll(vnfd.getMonitoring_parameter());
        virtualNetworkFunctionRecord.setVendor(vnfd.getVendor());
        virtualNetworkFunctionRecord.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
        for (AutoScalePolicy autoScalePolicy : vnfd.getAuto_scale_policy()) {
            AutoScalePolicy newAutoScalePolicy = new AutoScalePolicy();
            newAutoScalePolicy.setAction(autoScalePolicy.getAction());
            newAutoScalePolicy.setComparisonOperator(autoScalePolicy.getComparisonOperator());
            newAutoScalePolicy.setCooldown(autoScalePolicy.getCooldown());
            newAutoScalePolicy.setMetric(autoScalePolicy.getMetric());
            newAutoScalePolicy.setPeriod(autoScalePolicy.getPeriod());
            newAutoScalePolicy.setStatistic(autoScalePolicy.getStatistic());
            newAutoScalePolicy.setThreshold(autoScalePolicy.getThreshold());
            virtualNetworkFunctionRecord.getAuto_scale_policy().add(newAutoScalePolicy);
        }

        // TODO mange the VirtualLinks and links...
//        virtualNetworkFunctionRecord.setConnected_external_virtual_link(vnfd.getVirtual_link());

        virtualNetworkFunctionRecord.setVdu(new HashSet<VirtualDeploymentUnit>());
        for (VirtualDeploymentUnit virtualDeploymentUnit : vnfd.getVdu()) {
            VirtualDeploymentUnit vdu_new = new VirtualDeploymentUnit();
            HashSet<VNFComponent> vnfComponents = new HashSet<>();
            for (VNFComponent component : virtualDeploymentUnit.getVnfc()) {
                VNFComponent component_new = new VNFComponent();
                HashSet<VNFDConnectionPoint> connectionPoints = new HashSet<>();
                for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
                    VNFDConnectionPoint connectionPoint_new = new VNFDConnectionPoint();
                    connectionPoint_new.setName(connectionPoint.getName());
                    connectionPoint_new.setExtId(connectionPoint.getExtId());
                    connectionPoint_new.setVirtual_link_reference(connectionPoint.getVirtual_link_reference());
                    connectionPoint_new.setType(connectionPoint.getType());
                    connectionPoints.add(connectionPoint_new);
                }
                component_new.setConnection_point(connectionPoints);
                vnfComponents.add(component_new);
            }
            vdu_new.setVnfc(vnfComponents);
            HashSet<LifecycleEvent> lifecycleEvents = new HashSet<>();
            for (LifecycleEvent lifecycleEvent : virtualDeploymentUnit.getLifecycle_event()) {
                LifecycleEvent lifecycleEvent_new = new LifecycleEvent();
                lifecycleEvent_new.setEvent(lifecycleEvent.getEvent());
                lifecycleEvent_new.setLifecycle_events(lifecycleEvent.getLifecycle_events());
                lifecycleEvents.add(lifecycleEvent_new);
            }
            vdu_new.setLifecycle_event(lifecycleEvents);

            vdu_new.setHostname(virtualDeploymentUnit.getHostname());
            vdu_new.setHigh_availability(virtualDeploymentUnit.getHigh_availability());
            vdu_new.setExtId(virtualDeploymentUnit.getExtId());
            vdu_new.setComputation_requirement(virtualDeploymentUnit.getComputation_requirement());
            vdu_new.setScale_in_out(virtualDeploymentUnit.getScale_in_out());
            HashSet<String> monitoringParameters = new HashSet<>();
            monitoringParameters.addAll(virtualDeploymentUnit.getMonitoring_parameter());
            vdu_new.setMonitoring_parameter(monitoringParameters);
            vdu_new.setVdu_constraint(virtualDeploymentUnit.getVdu_constraint());

            HashSet<String> vmImages = new HashSet<>();
            vmImages.addAll(virtualDeploymentUnit.getVm_image());
            vdu_new.setVm_image(vmImages);

            vdu_new.setVirtual_network_bandwidth_resource(virtualDeploymentUnit.getVirtual_network_bandwidth_resource());
            vdu_new.setVirtual_memory_resource_element(virtualDeploymentUnit.getVirtual_memory_resource_element());
            vdu_new.setVimInstance(virtualDeploymentUnit.getVimInstance());
            virtualNetworkFunctionRecord.getVdu().add(vdu_new);
        }
        virtualNetworkFunctionRecord.setVersion(vnfd.getVersion());
        virtualNetworkFunctionRecord.setConnection_point(new HashSet<ConnectionPoint>());
        virtualNetworkFunctionRecord.getConnection_point().addAll(vnfd.getConnection_point());

        // TODO find a way to choose between deployment flavors and create the new one
        virtualNetworkFunctionRecord.setDeployment_flavour_key(vnfd.getDeployment_flavour().iterator().next().getFlavour_key());
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
            if (!existsDeploymentFlavor(virtualNetworkFunctionRecord.getDeployment_flavour_key(), virtualDeploymentUnit.getVimInstance())) {
                throw new BadFormatException("no key " + virtualNetworkFunctionRecord.getDeployment_flavour_key() + " found in vim instance: " + virtualDeploymentUnit.getVimInstance());
            }
        }

        virtualNetworkFunctionRecord.setDescriptor_reference(vnfd.getId());
        virtualNetworkFunctionRecord.setLifecycle_event(new LinkedHashSet<LifecycleEvent>());
        HashSet<LifecycleEvent> lifecycleEvents = new HashSet<>();
        for (LifecycleEvent lifecycleEvent : vnfd.getLifecycle_event()) {
            LifecycleEvent lifecycleEvent_new = new LifecycleEvent();
            lifecycleEvent_new.setEvent(lifecycleEvent.getEvent());
            lifecycleEvent_new.setLifecycle_events(new LinkedHashSet<String>());
            for (String event : lifecycleEvent.getLifecycle_events()) {
                lifecycleEvent_new.getLifecycle_events().add(event);
            }
            lifecycleEvents.add(lifecycleEvent_new);
        }
        virtualNetworkFunctionRecord.setLifecycle_event(lifecycleEvents);
        virtualNetworkFunctionRecord.setVirtual_link(new HashSet<InternalVirtualLink>());
        HashSet<InternalVirtualLink> internalVirtualLinks = new HashSet<>();
        for (InternalVirtualLink internalVirtualLink : vnfd.getVirtual_link()) {
            InternalVirtualLink internalVirtualLink_new = new InternalVirtualLink();
            internalVirtualLink_new.setLeaf_requirement(internalVirtualLink.getLeaf_requirement());
            internalVirtualLink_new.setRoot_requirement(internalVirtualLink.getRoot_requirement());
            internalVirtualLink_new.setConnection_points_references(new HashSet<String>());
            for (String conn : internalVirtualLink.getConnection_points_references()) {
                internalVirtualLink_new.getConnection_points_references().add(conn);
            }
            internalVirtualLink_new.setQos(new HashSet<String>());
            for (String qos : internalVirtualLink.getQos()) {
                internalVirtualLink_new.getQos().add(qos);
            }
            internalVirtualLink_new.setTest_access(new HashSet<String>());
            for (String test : internalVirtualLink.getTest_access()) {
                internalVirtualLink_new.getTest_access().add(test);
            }
            internalVirtualLink_new.setConnectivity_type(internalVirtualLink.getConnectivity_type());
            internalVirtualLinks.add(internalVirtualLink_new);
        }
        virtualNetworkFunctionRecord.getVirtual_link().addAll(internalVirtualLinks);

        virtualNetworkFunctionRecord.setVnf_address(new HashSet<String>());
        virtualNetworkFunctionRecord.setStatus(Status.NULL);
        return virtualNetworkFunctionRecord;
    }

    private static boolean existsDeploymentFlavor(String key, VimInstance vimInstance) {
        log.debug("" + vimInstance);
        for (DeploymentFlavour deploymentFlavour : vimInstance.getFlavours()) {
            if (deploymentFlavour.getFlavour_key().equals(key) || deploymentFlavour.getExtId().equals(key) || deploymentFlavour.getId().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public static void createConnectionsPoints(VirtualNetworkFunctionRecord vnfr, VirtualDeploymentUnit vdu, Network network) {
        //Create ConnectionPoint for VNFR
        ConnectionPoint connectionPoint = new ConnectionPoint();
        connectionPoint.setName(network.getName());
        connectionPoint.setExtId(network.getExtId());
        connectionPoint.setType("LAN");
        vnfr.getConnection_point().add(connectionPoint);
        //Create ConnectionPoint for VDU
        VNFDConnectionPoint vnfdConnectionPoint = new VNFDConnectionPoint();
        vnfdConnectionPoint.setVirtual_link_reference(network.getName());
        vnfdConnectionPoint.setName(network.getName());
        vnfdConnectionPoint.setExtId(network.getExtId());
        vnfdConnectionPoint.setType("LAN");
        //Create VNFC for VDU
        VNFComponent vnfComponent = new VNFComponent();
        vnfComponent.getConnection_point().add(vnfdConnectionPoint);
        vdu.getVnfc().add(vnfComponent);
    }

}
