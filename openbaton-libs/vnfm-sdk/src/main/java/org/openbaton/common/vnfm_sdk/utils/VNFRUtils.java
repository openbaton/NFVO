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

package org.openbaton.common.vnfm_sdk.utils;

import org.openbaton.catalogue.mano.common.*;
import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mob on 31.08.15.
 */
public class VNFRUtils {

    private static Logger log = LoggerFactory.getLogger(VNFRUtils.class);

    public static VirtualNetworkFunctionRecord createVirtualNetworkFunctionRecord(VirtualNetworkFunctionDescriptor vnfd, String flavourKey, String nsr_id, Set<VirtualLinkRecord> vlr, Collection<VimInstance> vimInstances) throws NotFoundException, BadFormatException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
        virtualNetworkFunctionRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
        virtualNetworkFunctionRecord.setParent_ns_id(nsr_id);
        virtualNetworkFunctionRecord.setName(vnfd.getName());
        virtualNetworkFunctionRecord.setType(vnfd.getType());
        Configuration configuration = new Configuration();
        if (vnfd.getConfigurations() != null) {
            configuration.setName(vnfd.getConfigurations().getName());
        } else
            configuration.setName(virtualNetworkFunctionRecord.getName());

        configuration.setConfigurationParameters(new HashSet<ConfigurationParameter>());
        if (vnfd.getConfigurations() != null) {
            for (ConfigurationParameter configurationParameter : vnfd.getConfigurations().getConfigurationParameters()) {
                ConfigurationParameter cp = new ConfigurationParameter();
                cp.setConfKey(configurationParameter.getConfKey());
                cp.setValue(configurationParameter.getValue());
                configuration.getConfigurationParameters().add(cp);
            }
        }
        virtualNetworkFunctionRecord.setConfigurations(configuration);
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

//        if (vnfd.getVnfPackageLocation() != null) {
//            VNFPackage vnfPackage = new VNFPackage();
//            vnfPackage.setImageLink(vnfd.getVnfPackageLocation().getImageLink());
//            vnfPackage.setScriptsLink(vnfd.getVnfPackageLocation().getScriptsLink());
//            vnfPackage.setName(vnfd.getVnfPackageLocation().getName());
//
//            //TODO check for ordering
//            vnfPackage.setScripts(new HashSet<Script>());
//
//            for (Script script : vnfd.getVnfPackageLocation().getScripts()) {
//                Script s = new Script();
//                s.setName(script.getName());
//                s.setPayload(script.getPayload());
//                vnfPackage.getScripts().add(s);
//            }
//
//            vnfPackage.setImage(vnfd.getVnfPackageLocation().getImage());
//        }
        virtualNetworkFunctionRecord.setPackageId(vnfd.getVnfPackageLocation());

        if (vnfd.getEndpoint() != null) {
            virtualNetworkFunctionRecord.setEndpoint(vnfd.getEndpoint());
        } else
            virtualNetworkFunctionRecord.setEndpoint(vnfd.getType());

        virtualNetworkFunctionRecord.setMonitoring_parameter(new HashSet<String>());
        virtualNetworkFunctionRecord.getMonitoring_parameter().addAll(vnfd.getMonitoring_parameter());
        virtualNetworkFunctionRecord.setVendor(vnfd.getVendor());
        virtualNetworkFunctionRecord.setAuto_scale_policy(new HashSet<AutoScalePolicy>());
        for (AutoScalePolicy autoScalePolicy : vnfd.getAuto_scale_policy()) {
            AutoScalePolicy newAutoScalePolicy = new AutoScalePolicy();
            newAutoScalePolicy.setName(autoScalePolicy.getName());
            newAutoScalePolicy.setType(autoScalePolicy.getType());
            newAutoScalePolicy.setCooldown(autoScalePolicy.getCooldown());
            newAutoScalePolicy.setPeriod(autoScalePolicy.getPeriod());
            newAutoScalePolicy.setComparisonOperator(autoScalePolicy.getComparisonOperator());
            newAutoScalePolicy.setThreshold(autoScalePolicy.getThreshold());
            newAutoScalePolicy.setMode(autoScalePolicy.getMode());
            newAutoScalePolicy.setActions(new HashSet<ScalingAction>());
            for (ScalingAction action : autoScalePolicy.getActions()) {
                ScalingAction newAction = new ScalingAction();
                newAction.setValue(action.getValue());
                newAction.setType(action.getType());
                newAutoScalePolicy.getActions().add(newAction);
            }
            newAutoScalePolicy.setAlarms(new HashSet<ScalingAlarm>());
            for (ScalingAlarm alarm : autoScalePolicy.getAlarms()) {
                ScalingAlarm newAlarm = new ScalingAlarm();
                newAlarm.setComparisonOperator(alarm.getComparisonOperator());
                newAlarm.setMetric(alarm.getMetric());
                newAlarm.setStatistic(alarm.getStatistic());
                newAlarm.setThreshold(alarm.getThreshold());
                newAlarm.setWeight(alarm.getWeight());
                newAutoScalePolicy.getAlarms().add(newAlarm);
            }
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
                    connectionPoint_new.setVirtual_link_reference(connectionPoint.getVirtual_link_reference());
                    connectionPoint_new.setType(connectionPoint.getType());
                    connectionPoint_new.setFloatingIp(connectionPoint.getFloatingIp());
                    connectionPoints.add(connectionPoint_new);
                }
                component_new.setConnection_point(connectionPoints);
                vnfComponents.add(component_new);
            }
            vdu_new.setVnfc(vnfComponents);
            vdu_new.setVnfc_instance(new HashSet<VNFCInstance>());
            HashSet<LifecycleEvent> lifecycleEvents = new HashSet<>();
            for (LifecycleEvent lifecycleEvent : virtualDeploymentUnit.getLifecycle_event()) {
                LifecycleEvent lifecycleEvent_new = new LifecycleEvent();
                lifecycleEvent_new.setEvent(lifecycleEvent.getEvent());
                lifecycleEvent_new.setLifecycle_events(lifecycleEvent.getLifecycle_events());
                lifecycleEvents.add(lifecycleEvent_new);
            }
            vdu_new.setLifecycle_event(lifecycleEvents);
            vdu_new.setVimInstanceName(virtualDeploymentUnit.getVimInstanceName());
            vdu_new.setHostname(virtualDeploymentUnit.getHostname());
            vdu_new.setHigh_availability(virtualDeploymentUnit.getHigh_availability());
            vdu_new.setComputation_requirement(virtualDeploymentUnit.getComputation_requirement());
            vdu_new.setScale_in_out(virtualDeploymentUnit.getScale_in_out());
            HashSet<String> monitoringParameters = new HashSet<>();
            monitoringParameters.addAll(virtualDeploymentUnit.getMonitoring_parameter());
            vdu_new.setMonitoring_parameter(monitoringParameters);
            vdu_new.setVdu_constraint(virtualDeploymentUnit.getVdu_constraint());

            //Set Faultmanagement policies
            Set<VRFaultManagementPolicy> vrFaultManagementPolicies = new HashSet<>();
            if (virtualDeploymentUnit.getFault_management_policy() != null) {
                log.debug("Adding the fault management policies: " + virtualDeploymentUnit.getFault_management_policy());
                for (VRFaultManagementPolicy vrfmp : virtualDeploymentUnit.getFault_management_policy()) {
                    vrFaultManagementPolicies.add(vrfmp);
                }
            }
            vdu_new.setFault_management_policy(vrFaultManagementPolicies);
            //Set Faultmanagement policies end

            HashSet<String> vmImages = new HashSet<>();
            vmImages.addAll(virtualDeploymentUnit.getVm_image());
            vdu_new.setVm_image(vmImages);

            vdu_new.setVirtual_network_bandwidth_resource(virtualDeploymentUnit.getVirtual_network_bandwidth_resource());
            vdu_new.setVirtual_memory_resource_element(virtualDeploymentUnit.getVirtual_memory_resource_element());
            virtualNetworkFunctionRecord.getVdu().add(vdu_new);
        }
        virtualNetworkFunctionRecord.setVersion(vnfd.getVersion());
        virtualNetworkFunctionRecord.setConnection_point(new HashSet<ConnectionPoint>());
        virtualNetworkFunctionRecord.getConnection_point().addAll(vnfd.getConnection_point());

        // TODO find a way to choose between deployment flavors and create the new one
        virtualNetworkFunctionRecord.setDeployment_flavour_key(flavourKey);
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
            for (VimInstance vi : vimInstances) {
                for (String name : virtualDeploymentUnit.getVimInstanceName()) {
                    if (name.equals(vi.getName())) {
                        if (!existsDeploymentFlavor(virtualNetworkFunctionRecord.getDeployment_flavour_key(), vi)) {
                            throw new BadFormatException("no key " + virtualNetworkFunctionRecord.getDeployment_flavour_key() + " found in vim instance: " + vi);
                        }
                    }
                }
            }
        }

        virtualNetworkFunctionRecord.setDescriptor_reference(vnfd.getId());
        virtualNetworkFunctionRecord.setLifecycle_event(new LinkedHashSet<LifecycleEvent>());
        HashSet<LifecycleEvent> lifecycleEvents = new HashSet<>();
        for (LifecycleEvent lifecycleEvent : vnfd.getLifecycle_event()) {
            LifecycleEvent lifecycleEvent_new = new LifecycleEvent();
            lifecycleEvent_new.setEvent(lifecycleEvent.getEvent());
            lifecycleEvent_new.setLifecycle_events(new ArrayList<String>());
            for (String event : lifecycleEvent.getLifecycle_events()) {
                lifecycleEvent_new.getLifecycle_events().add(event);
            }
            log.debug("Found SCRIPTS for EVENT " + lifecycleEvent_new.getEvent() + ": " + lifecycleEvent_new.getLifecycle_events().size());
            lifecycleEvents.add(lifecycleEvent_new);
        }
        virtualNetworkFunctionRecord.setLifecycle_event(lifecycleEvents);
        virtualNetworkFunctionRecord.setVirtual_link(new HashSet<InternalVirtualLink>());
        HashSet<InternalVirtualLink> internalVirtualLinks = new HashSet<>();
        for (InternalVirtualLink internalVirtualLink : vnfd.getVirtual_link()) {
            InternalVirtualLink internalVirtualLink_new = new InternalVirtualLink();
            internalVirtualLink_new.setName(internalVirtualLink.getName());

            for (VirtualLinkRecord virtualLinkRecord : vlr) {
                if (virtualLinkRecord.getName().equals(internalVirtualLink_new.getName())) {
                    internalVirtualLink_new.setExtId(virtualLinkRecord.getExtId());
                }
            }

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
}
