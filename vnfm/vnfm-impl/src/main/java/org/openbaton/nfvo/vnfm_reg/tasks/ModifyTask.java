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
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.nfvo.repositories.VNFRDependencyRepository;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ModifyTask extends AbstractTask {

    @Autowired
    private VnfmRegister vnfmRegister;

    @Autowired
    private VNFRDependencyRepository vnfrDependencyRepository;

    @Override
    protected void doWork() throws Exception {
        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());
        virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
        log.debug("NFVO: MODIFY finish");
        log.trace("VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        saveVirtualNetworkFunctionRecord();
        log.trace("Now VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
        log.debug("VNFR Status is: " + virtualNetworkFunctionRecord.getStatus());
        vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.START), vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
