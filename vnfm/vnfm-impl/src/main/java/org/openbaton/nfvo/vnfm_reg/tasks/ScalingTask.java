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
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ScalingTask extends AbstractTask {
    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;
    @Autowired
    private ResourceManagement resourceManagement;

    @Override
    protected void doWork() throws Exception {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());

        log.debug("NFVO: SCALING");
        log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
        log.debug("The VNFR: " + virtualNetworkFunctionRecord.getName() + " is in status --> " + virtualNetworkFunctionRecord.getStatus());

        saveVirtualNetworkFunctionRecord();

        VNFComponent componentToAdd = null;
        VirtualDeploymentUnit vdu = null;
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()){
            for (VNFComponent vnfComponent : virtualDeploymentUnit.getVnfc()) {
                boolean found = false;
                for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                    if (vnfComponent.getId().equals(vnfcInstance.getVnfComponent().getId())){
                        found = true;
                        break;
                    }
                }
                if (!found){ // new vnfComponent!
                    componentToAdd = vnfComponent;
                    vdu = virtualDeploymentUnit;
                    break;
                }
            }
        }

        log.debug("The component to add is: " + componentToAdd);

        log.debug("Added new component with id: " + resourceManagement.allocate(vdu, virtualNetworkFunctionRecord, componentToAdd));

        log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
        vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.SCALED), getTempDestination());
    }



    @Override
    public boolean isAsync() {
        return true;
    }
}
