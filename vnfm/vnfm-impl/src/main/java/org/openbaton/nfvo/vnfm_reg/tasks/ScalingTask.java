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

import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
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
public class ScalingTask extends AbstractTask {
    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    @Override
    protected void doWork() throws Exception {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        log.debug("NFVO: SCALING");
        log.debug("The VNFR: " + virtualNetworkFunctionRecord.getName() + " shoud be in status scaling --> " + virtualNetworkFunctionRecord.getStatus());
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord_nfvo = vnfrRepository.findOne(virtualNetworkFunctionRecord.getId());
        virtualNetworkFunctionRecord_nfvo.setStatus(Status.SCALING);
        //Updating VDUs
        Set<VirtualDeploymentUnit> vdus = new HashSet<>();
        boolean found = false;
        for (VirtualDeploymentUnit vdu_manager : virtualNetworkFunctionRecord.getVdu()) {
            //VDU ID is null -> NEW
            if (vdu_manager.getId() == null) {
                vdus.add(vdu_manager);
                log.debug("SCALING: Added new VDU " + vdu_manager);
                continue;
            }
            for (VirtualDeploymentUnit vdu_nfvo : virtualNetworkFunctionRecord_nfvo.getVdu()) {
                //Found VDU -> Updating
                if (vdu_nfvo.getId().equals(vdu_manager.getId())) {
                    found = true;
                    log.debug("SCALING: Updating VDU " + vdu_nfvo.getId());
                    vdu_nfvo.setVimInstance(vdu_manager.getVimInstance());
                    vdu_nfvo.setComputation_requirement(vdu_manager.getComputation_requirement());
                    vdu_nfvo.setHigh_availability(vdu_manager.getHigh_availability());
                    vdu_nfvo.setScale_in_out(vdu_manager.getScale_in_out());
                    vdu_nfvo.setVdu_constraint(vdu_manager.getVdu_constraint());
                    vdu_nfvo.setVirtual_memory_resource_element(vdu_manager.getVirtual_memory_resource_element());
                    vdu_nfvo.setVirtual_network_bandwidth_resource(vdu_manager.getVirtual_network_bandwidth_resource());
                    vdu_nfvo.setVm_image(vdu_manager.getVm_image());
                    //Updating VNFComponents
                    vdu_nfvo.setVnfc(updateVNFComponents(vdu_nfvo.getVnfc(), vdu_manager.getVnfc()));
                    log.debug("SCALING: VNFComponents of VDU " + vdu_nfvo.getId() + ": " + vdu_nfvo.getVnfc());
                    vdus.add(vdu_nfvo);
                    break;
                }
            }
            //VDU was not found -> NEW
            if (!found) {
                vdus.add(vdu_manager);
                log.debug("SCALING: Added new VDU " + vdu_manager.getId());
            }
        }
        virtualNetworkFunctionRecord_nfvo.setVdu(vdus);
        log.debug("SCALING: VDUs of VNFR " + virtualNetworkFunctionRecord_nfvo.getId() + ": " + vdus);
        virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord_nfvo);
        log.info("SCALING: Finished with VNFR: " + virtualNetworkFunctionRecord_nfvo);
        vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.SCALING), getTempDestination());
    }

    private Set<VNFComponent> updateVNFComponents(Set<VNFComponent> vnfComponents_nfvo, Set<VNFComponent> vnfComponents_manager) {
        Set<VNFComponent> components = new HashSet<>();
        boolean found = false;
        //Updating existing Components, adding new ones and ignoring old ones
        for (VNFComponent vnfComponent_manager : vnfComponents_manager) {
            //VNFCInstance ID is null -> NEW
            if (vnfComponent_manager.getId() == null) {
                components.add(vnfComponent_manager);
                log.debug("SCALING: Added new VNFComponent " + vnfComponent_manager);
                continue;
            }
            for (VNFComponent vnfComponent_nfvo : vnfComponents_nfvo) {
                //Found Instance -> Updating
                if (vnfComponent_nfvo.getId().equals(vnfComponent_manager.getId())) {
                    log.debug("SCALING: Updating exsting VNFComponent " + vnfComponent_nfvo.getId());
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
                log.debug("SCALING: Added new VNFComponent " + vnfComponent_manager.getId());
            }
        }
        log.debug("SCALING: Updated VNFComponents " + components);
        return components;
    }

    private Set<VNFDConnectionPoint> updateVNFDConnectionPoints(Set<VNFDConnectionPoint> vnfdConnectionPoints_nfvo, Set<VNFDConnectionPoint> vnfdConnectionPoints_manager) {
        Set<VNFDConnectionPoint> vnfdConnectionPoints = new HashSet<>();
        boolean found = false;
        //Updating existing VNFDConnectionPoints, adding new ones and ignoring old ones
        for (VNFDConnectionPoint vnfdConnectionPoint_manager : vnfdConnectionPoints_manager) {
            //VNFDConnectionPoint ID is null -> NEW
            if (vnfdConnectionPoint_manager.getId() == null) {
                vnfdConnectionPoints.add(vnfdConnectionPoint_manager);
                log.debug("SCALING: Added new VNFDConnectionPoint " + vnfdConnectionPoint_manager);
                continue;
            }
            for (VNFDConnectionPoint vnfdConnectionPoint_nfvo : vnfdConnectionPoints_nfvo) {
                //Found VNFDConnectionPoint -> Updating
                if (vnfdConnectionPoint_nfvo.getId().equals(vnfdConnectionPoint_manager.getId())) {
                    found = true;
                    log.debug("SCALING: Updating exsting VNFDConnectionPoint " + vnfdConnectionPoint_nfvo.getId());
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
                log.debug("SCALING: Added new VNFDConnectionPoint " + vnfdConnectionPoint_manager.getId());
            }
        }
        log.debug("SCALING: Updated VNFDConnectionPoints " + vnfdConnectionPoints);
        return vnfdConnectionPoints;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
