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
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class AllocateresourcesTask extends AbstractTask {
  @Autowired private ResourceManagement resourceManagement;
  private Map<String, VimInstance> vims;
  private String userData;

  @Override
  protected NFVMessage doWork() throws Exception {

    log.info(
        "Executing task: AllocateResources for VNFR: " + virtualNetworkFunctionRecord.getName());
    log.debug("Verison is: " + virtualNetworkFunctionRecord.getHb_version());
    try {
      for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
        List<Future<List<String>>> ids = new ArrayList<>();
        VimInstance vimInstance = vims.get(vdu.getId());
        if (vimInstance == null)
          throw new NullPointerException(
              "Our algorithms are too complex, even for us, this is what abnormal IQ means :(");
        try {
          ids.add(
              resourceManagement.allocate(
                  vdu, virtualNetworkFunctionRecord, vimInstance, userData));

          for (Future<List<String>> id : ids) {
            id.get();
          }
        } catch (VimException e) {
          e.printStackTrace();
          log.error(e.getMessage());
          LifecycleEvent lifecycleEvent = new LifecycleEvent();
          lifecycleEvent.setEvent(Event.ERROR);
          VNFCInstance vnfcInstance = e.getVnfcInstance();

          if (vnfcInstance != null) {
            log.info("The VM was not correctly deployed. ExtId is: " + vnfcInstance.getVc_id());
            log.debug("Details are: " + vnfcInstance);
            vdu.getVnfc_instance().add(vnfcInstance);
          }
          virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
          saveVirtualNetworkFunctionRecord();
          return new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
        }
      }
    } catch (VimDriverException e) {
      e.printStackTrace();
      log.error(e.getMessage());
      LifecycleEvent lifecycleEvent = new LifecycleEvent();
      lifecycleEvent.setEvent(Event.ERROR);
      virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
      virtualNetworkFunctionRecord.setStatus(Status.ERROR);
      saveVirtualNetworkFunctionRecord();
      return new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
    }

    for (LifecycleEvent event : virtualNetworkFunctionRecord.getLifecycle_event()) {
      if (event.getEvent().ordinal() == Event.ALLOCATE.ordinal()) {
        virtualNetworkFunctionRecord.getLifecycle_event_history().add(event);
        break;
      }
    }
    saveVirtualNetworkFunctionRecord();

    OrVnfmGenericMessage orVnfmGenericMessage =
        new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.ALLOCATE_RESOURCES);
    log.debug("Answering to RPC allocate resources: " + orVnfmGenericMessage);
    log.info(
        "Finished task: AllocateResources for VNFR: " + virtualNetworkFunctionRecord.getName());
    return orVnfmGenericMessage;
  }

  @Override
  public boolean isAsync() {
    return true;
  }

  public void setVims(Map<String, VimInstance> vimChosen) {
    this.vims = vimChosen;
  }

  public void setUserData(String userData) {
    this.userData = userData;
  }
}
