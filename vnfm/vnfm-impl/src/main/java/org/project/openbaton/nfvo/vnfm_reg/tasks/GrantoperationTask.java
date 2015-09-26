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

package org.project.openbaton.nfvo.vnfm_reg.tasks;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.project.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.project.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class GrantoperationTask extends AbstractTask {

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    @Autowired
    private VNFLifecycleOperationGranting lifecycleOperationGranting;

    @Override
    protected void doWork() throws Exception {

        VnfmSender vnfmSender;
        vnfmSender = this.getVnfmSender(vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());
        if (lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord)) {
            LifecycleEvent lifecycleEvent = new LifecycleEvent();
            lifecycleEvent.setEvent(Event.GRANTED);
            lifecycleEvent.setLifecycle_events(new LinkedList<String>());
            if (virtualNetworkFunctionRecord.getLifecycle_event_history() == null)
                virtualNetworkFunctionRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
            virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
            log.debug("SENDING GRANT LIFECYCLE OPERATION on temp queue:" + getTempDestination());
            saveVirtualNetworkFunctionRecord();
            log.debug("HIBERNATE VERSION IS: " + virtualNetworkFunctionRecord.getHb_version());
            vnfmSender.sendCommand(new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.GRANT_OPERATION), getTempDestination());
        } else {
            // there are not enough resources for deploying VNFR
            saveVirtualNetworkFunctionRecord();
            vnfmSender.sendCommand(new OrVnfmErrorMessage(virtualNetworkFunctionRecord, "Not enough resources for deploying VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName()), getTempDestination());
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
