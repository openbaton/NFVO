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

import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties
public class GrantoperationTask extends AbstractTask {

    public boolean isCheckQuota() {
        return checkQuota;
    }

    public void setCheckQuota(boolean checkQuota) {
        this.checkQuota = checkQuota;
    }

    @Value("${nfvo.quota.check:true}")
    private boolean checkQuota;

    @Autowired
    private VNFLifecycleOperationGranting lifecycleOperationGranting;

    @Override
    protected NFVMessage doWork() throws Exception {
        log.info("Executing task: GrantOperation on VNFR: " + virtualNetworkFunctionRecord.getName());

        if (!checkQuota) {
            log.warn("Checking quota is disabled, please consider to enable it");
            LifecycleEvent lifecycleEvent = new LifecycleEvent();
            lifecycleEvent.setEvent(Event.GRANTED);
            lifecycleEvent.setLifecycle_events(new ArrayList<String>());
            if (virtualNetworkFunctionRecord.getLifecycle_event_history() == null)
                virtualNetworkFunctionRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
            virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
            saveVirtualNetworkFunctionRecord();
            log.debug("Hibernate version is: " + virtualNetworkFunctionRecord.getHb_version());
            OrVnfmGenericMessage nfvMessage = new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.GRANT_OPERATION);
            return nfvMessage;
        }
        else{
            if (lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord)) {
                log.info("Finished task: GrantOperation on VNFR: " + virtualNetworkFunctionRecord.getName());
                LifecycleEvent lifecycleEvent = new LifecycleEvent();
                lifecycleEvent.setEvent(Event.GRANTED);
                lifecycleEvent.setLifecycle_events(new ArrayList<String>());
                if (virtualNetworkFunctionRecord.getLifecycle_event_history() == null)
                    virtualNetworkFunctionRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
                virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
                saveVirtualNetworkFunctionRecord();
                log.debug("Hibernate version is: " + virtualNetworkFunctionRecord.getHb_version());
                OrVnfmGenericMessage nfvMessage = new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.GRANT_OPERATION);
                return nfvMessage;
            } else {
                // there are not enough resources for deploying VNFR
                log.error("Not enough resources for deploying VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName());
                virtualNetworkFunctionRecord.setStatus(Status.ERROR);
                saveVirtualNetworkFunctionRecord();
                vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
                OrVnfmErrorMessage nfvMessage = new OrVnfmErrorMessage(virtualNetworkFunctionRecord, "Not enough resources for deploying VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName());
                return nfvMessage;
            }
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
