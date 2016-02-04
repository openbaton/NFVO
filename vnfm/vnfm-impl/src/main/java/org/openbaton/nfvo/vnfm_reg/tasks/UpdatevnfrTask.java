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

package org.openbaton.nfvo.vnfm_reg.tasks;

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class UpdatevnfrTask extends AbstractTask {
    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    @Override
    protected NFVMessage doWork() throws Exception {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        log.info("Updating VNFR " + virtualNetworkFunctionRecord.getName());
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord_nfvo = vnfrRepository.findOne(virtualNetworkFunctionRecord.getId());
        //Updating VNFR
        virtualNetworkFunctionRecord_nfvo.setStatus(virtualNetworkFunctionRecord.getStatus());
        virtualNetworkFunctionRecord_nfvo.setName(virtualNetworkFunctionRecord.getName());
        virtualNetworkFunctionRecord_nfvo.setDeployment_flavour_key(virtualNetworkFunctionRecord.getDeployment_flavour_key());
        virtualNetworkFunctionRecord_nfvo.setTask(virtualNetworkFunctionRecord.getTask());
        virtualNetworkFunctionRecord_nfvo.setType(virtualNetworkFunctionRecord.getType());
        virtualNetworkFunctionRecord_nfvo.setAudit_log(virtualNetworkFunctionRecord.getAudit_log());
        virtualNetworkFunctionRecord_nfvo.setLocalization(virtualNetworkFunctionRecord.getLocalization());
        virtualNetworkFunctionRecord_nfvo.setMonitoring_parameter(virtualNetworkFunctionRecord.getMonitoring_parameter());
        virtualNetworkFunctionRecord_nfvo.setNotification(virtualNetworkFunctionRecord.getNotification());
        virtualNetworkFunctionRecord_nfvo.setVnf_address(virtualNetworkFunctionRecord.getVnf_address());
        virtualNetworkFunctionRecord_nfvo.setRuntime_policy_info(virtualNetworkFunctionRecord.getRuntime_policy_info());
        virtualNetworkFunctionRecord_nfvo.setProvides(updateConfiguration(virtualNetworkFunctionRecord_nfvo.getProvides(), virtualNetworkFunctionRecord.getProvides()));
        virtualNetworkFunctionRecord_nfvo.setRequires(updateConfiguration(virtualNetworkFunctionRecord_nfvo.getRequires(), virtualNetworkFunctionRecord.getRequires()));
        virtualNetworkFunctionRecord_nfvo.setConfigurations(updateConfiguration(virtualNetworkFunctionRecord_nfvo.getConfigurations(), virtualNetworkFunctionRecord.getConfigurations()));
        //TODO Update more parameters
        //Updating VDUs
        Set<VirtualDeploymentUnit> vdus = new HashSet<>();
        boolean found = false;
        for (VirtualDeploymentUnit vdu_manager : virtualNetworkFunctionRecord.getVdu()) {
            //VDU ID is null -> NEW
            if (vdu_manager.getId() == null) {
                vdus.add(vdu_manager);
                log.debug("Update: Added new VDU " + vdu_manager);
                continue;
            }
            for (VirtualDeploymentUnit vdu_nfvo : virtualNetworkFunctionRecord_nfvo.getVdu()) {
                //Found VDU -> Updating
                if (vdu_nfvo.getId().equals(vdu_manager.getId())) {
                    found = true;
                    log.debug("Update: Updating VDU " + vdu_nfvo.getId());
                    vdu_nfvo.setComputation_requirement(vdu_manager.getComputation_requirement());
                    vdu_nfvo.setHigh_availability(vdu_manager.getHigh_availability());
                    vdu_nfvo.setScale_in_out(vdu_manager.getScale_in_out());
                    vdu_nfvo.setVdu_constraint(vdu_manager.getVdu_constraint());
                    vdu_nfvo.setVirtual_memory_resource_element(vdu_manager.getVirtual_memory_resource_element());
                    vdu_nfvo.setVirtual_network_bandwidth_resource(vdu_manager.getVirtual_network_bandwidth_resource());
                    vdu_nfvo.setVm_image(vdu_manager.getVm_image());
                    //Updating VNFComponents
                    vdu_nfvo.setVnfc(updateVNFComponents(vdu_nfvo.getVnfc(), vdu_manager.getVnfc()));
                    log.debug("Update: VNFComponents of VDU " + vdu_nfvo.getId() + ": " + vdu_nfvo.getVnfc());
                    //Updating VNFCInstances
                    vdu_nfvo.setVnfc_instance(updateVNFCInstances(vdu_nfvo.getVnfc_instance(), vdu_manager.getVnfc_instance()));
                    log.debug("Update: VNFCInstances of VDU " + vdu_nfvo.getId() + ": " + vdu_nfvo.getVnfc_instance());
                    vdus.add(vdu_nfvo);
                    break;
                }
            }
            //VDU was not found -> NEW
            if (!found) {
                vdus.add(vdu_manager);
                log.debug("Update: Added new VDU " + vdu_manager.getId());
            }
        }
        virtualNetworkFunctionRecord_nfvo.setVdu(vdus);
        log.debug("Update: VDUs of VNFR " + virtualNetworkFunctionRecord_nfvo.getId() + ": " + vdus);
        virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord_nfvo);
        log.info("Update: Finished with VNFR: " + virtualNetworkFunctionRecord_nfvo.getName());
        OrVnfmGenericMessage nfvMessage = new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.UPDATEVNFR);
//        vnfmSender.sendCommand(nfvMessage, getTempDestination());
        return nfvMessage;
    }

    private Set<VNFComponent> updateVNFComponents(Set<VNFComponent> vnfComponents_nfvo, Set<VNFComponent> vnfComponents_manager) {
        Set<VNFComponent> components = new HashSet<>();
        boolean found = false;
        //Updating existing Components, adding new ones and ignoring old ones
        for (VNFComponent vnfComponent_manager : vnfComponents_manager) {
            //VNFCInstance ID is null -> NEW
            if (vnfComponent_manager.getId() == null) {
                components.add(vnfComponent_manager);
                log.debug("Update: Added new VNFComponent " + vnfComponent_manager);
                continue;
            }
            for (VNFComponent vnfComponent_nfvo : vnfComponents_nfvo) {
                //Found Instance -> Updating
                if (vnfComponent_nfvo.getId().equals(vnfComponent_manager.getId())) {
                    log.debug("Update: Updating exsting VNFComponent " + vnfComponent_nfvo.getId());
                    found = true;
                    //Update Instance
                    vnfComponent_nfvo.setConnection_point(updateVNFDConnectionPoints(vnfComponent_nfvo.getConnection_point(), vnfComponent_manager.getConnection_point()));
                    //Add updated VNFCInstance
                    components.add(vnfComponent_nfvo);
                    //Proceed with the next VNFCInstance
                    break;
                }
            }
            //VNFCInstance was not found -> NEW
            if (!found) {
                components.add(vnfComponent_manager);
                log.debug("Update: Added new VNFComponent " + vnfComponent_manager.getId());
            }
        }
        log.debug("Update: Updated VNFComponents " + components);
        return components;
    }

    private Set<VNFCInstance> updateVNFCInstances(Set<VNFCInstance> vnfcInstances_nfvo, Set<VNFCInstance> vnfcInstances_manager) {
        Set<VNFCInstance> instances = new HashSet<>();
        boolean found = false;
        //Updating existing Instances, adding new ones and ignoring old ones
        for (VNFCInstance vnfcInstance_manager : vnfcInstances_manager) {
            //VNFCInstance ID is null -> NEW
            if (vnfcInstance_manager.getId() == null) {
                instances.add(vnfcInstance_manager);
                log.debug("Update: Added new VNFCInstance " + vnfcInstance_manager);
                continue;
            }
            for (VNFCInstance vnfcInstance_nfvo : vnfcInstances_nfvo) {
                //Found Instance -> Updating
                if (vnfcInstance_nfvo.getId().equals(vnfcInstance_manager.getId())) {
                    log.debug("Update: Updating existing VNFCInstance " + vnfcInstance_nfvo.getId());
                    found = true;
                    //Updating Instance
                    vnfcInstance_nfvo.setHostname(vnfcInstance_manager.getHostname());
                    vnfcInstance_nfvo.setVim_id(vnfcInstance_manager.getVim_id());
                    vnfcInstance_nfvo.setVc_id(vnfcInstance_manager.getVc_id());
                    vnfcInstance_nfvo.setVnfComponent(vnfcInstance_manager.getVnfComponent());
                    vnfcInstance_nfvo.setConnection_point(updateVNFDConnectionPoints(vnfcInstance_nfvo.getConnection_point(), vnfcInstance_manager.getConnection_point()));
                    //Add updated VNFCInstance
                    instances.add(vnfcInstance_nfvo);
                    //Proceed with the next VNFCInstance
                    break;
                }
            }
            //VNFCInstance was not found -> NEW
            if (!found) {
                instances.add(vnfcInstance_manager);
                log.debug("Update: Added new Component " + vnfcInstance_manager.getId());
            }
        }
        log.debug("Update: Updated VNFCInstances " + instances);
        return instances;
    }

    private Configuration updateConfiguration(Configuration configuration_nfvo, Configuration configuration_manager) {
        configuration_nfvo.setName(configuration_manager.getName());
        configuration_nfvo.setConfigurationParameters(updateConfigurationParameters(configuration_nfvo.getConfigurationParameters(), configuration_manager.getConfigurationParameters()));
        return configuration_nfvo;
    }

    private Set<ConfigurationParameter> updateConfigurationParameters(Set<ConfigurationParameter> configurationParameters_nfvo, Set<ConfigurationParameter> configurationParameters_manager) {
        Set<ConfigurationParameter> configurationParameters = new HashSet<>();
        boolean found = false;
        //Updating existing Components, adding new ones and ignoring old ones
        for (ConfigurationParameter configurationParameter_manager : configurationParameters_manager) {
            //VNFCInstance ID is null -> NEW
            if (configurationParameter_manager.getId() == null) {
                configurationParameters.add(configurationParameter_manager);
                log.debug("Update: Added new ConfigurationParameter " + configurationParameter_manager);
                continue;
            }
            for (ConfigurationParameter configurationParameter_nfvo : configurationParameters_nfvo) {
                //Found Instance -> Updating
                if (configurationParameter_nfvo.getId().equals(configurationParameter_manager.getId())) {
                    log.debug("Update: Updating existing ConfigurationParameter " + configurationParameter_nfvo.getId());
                    found = true;
                    configurationParameter_nfvo.setConfKey(configurationParameter_manager.getConfKey());
                    configurationParameter_nfvo.setValue(configurationParameter_manager.getValue());
                    //Add updated ConfigurationParameter
                    configurationParameters.add(configurationParameter_nfvo);
                    //Proceed with the next ConfigurationParameter
                    break;
                }
            }
            //ConfigurationParameter was not found -> NEW
            if (!found) {
                configurationParameters.add(configurationParameter_manager);
                log.debug("Update: Added new ConfigurationParameter " + configurationParameter_manager.getId());
            }
        }
        log.debug("Update: Updated ConfigurationParameters " + configurationParameters);
        return configurationParameters;
    }


    private Set<VNFDConnectionPoint> updateVNFDConnectionPoints(Set<VNFDConnectionPoint> vnfdConnectionPoints_nfvo, Set<VNFDConnectionPoint> vnfdConnectionPoints_manager) {
        Set<VNFDConnectionPoint> vnfdConnectionPoints = new HashSet<>();
        boolean found = false;
        //Updating existing VNFDConnectionPoints, adding new ones and ignoring old ones
        for (VNFDConnectionPoint vnfdConnectionPoint_manager : vnfdConnectionPoints_manager) {
            //VNFDConnectionPoint ID is null -> NEW
            if (vnfdConnectionPoint_manager.getId() == null) {
                vnfdConnectionPoints.add(vnfdConnectionPoint_manager);
                log.debug("Update: Added new VNFDConnectionPoint " + vnfdConnectionPoint_manager);
                continue;
            }
            for (VNFDConnectionPoint vnfdConnectionPoint_nfvo : vnfdConnectionPoints_nfvo) {
                //Found VNFDConnectionPoint -> Updating
                if (vnfdConnectionPoint_nfvo.getId().equals(vnfdConnectionPoint_manager.getId())) {
                    found = true;
                    log.debug("Update: Updating existing VNFDConnectionPoint " + vnfdConnectionPoint_nfvo.getId());
                    vnfdConnectionPoint_nfvo.setVirtual_link_reference(vnfdConnectionPoint_manager.getVirtual_link_reference());
                    vnfdConnectionPoint_nfvo.setType(vnfdConnectionPoint_manager.getType());
                    //Add updated VNFDConnectionPoint
                    vnfdConnectionPoints.add(vnfdConnectionPoint_nfvo);
                    //Proceed with the next VNFDConnectionPoint
                    break;
                }
            }
            //VNFDConnectionPoint not found -> NEW
            if (!found) {
                vnfdConnectionPoints.add(vnfdConnectionPoint_manager);
                log.debug("Update: Added new VNFDConnectionPoint " + vnfdConnectionPoint_manager.getId());
            }
        }
        log.debug("Update: Updated VNFDConnectionPoints " + vnfdConnectionPoints);
        return vnfdConnectionPoints;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
