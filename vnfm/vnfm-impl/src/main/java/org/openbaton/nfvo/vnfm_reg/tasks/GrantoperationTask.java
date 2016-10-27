/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
 *
 */

package org.openbaton.nfvo.vnfm_reg.tasks;

import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGrantLifecycleOperationMessage;
import org.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.openbaton.nfvo.core.interfaces.VnfPlacementManagement;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties
public class GrantoperationTask extends AbstractTask {

  @Autowired private VnfPlacementManagement vnfPlacementManagement;

  public boolean isCheckQuota() {
    return checkQuota;
  }

  public void setCheckQuota(boolean checkQuota) {
    this.checkQuota = checkQuota;
  }

  @Value("${nfvo.quota.check:true}")
  private boolean checkQuota;

  @Autowired private VNFLifecycleOperationGranting lifecycleOperationGranting;

  @Override
  protected NFVMessage doWork() throws Exception {
    log.info("Executing task: GrantOperation on VNFR: " + virtualNetworkFunctionRecord.getName());

    if (!checkQuota) {
      log.warn("Checking quota is disabled, please consider to enable it");
      setHistoryLifecycleEvent(new Date());
      saveVirtualNetworkFunctionRecord();
      log.debug("Hibernate version is: " + virtualNetworkFunctionRecord.getHb_version());
      OrVnfmGrantLifecycleOperationMessage nfvMessage = new OrVnfmGrantLifecycleOperationMessage();
      nfvMessage.setGrantAllowed(true);
      nfvMessage.setVduVim(new HashMap<String, VimInstance>());
      for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
        nfvMessage
            .getVduVim()
            .put(
                virtualDeploymentUnit.getId(),
                vnfPlacementManagement.choseRandom(virtualDeploymentUnit.getVimInstanceName()));
      }
      nfvMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
      //                OrVnfmGenericMessage nfvMessage = new OrVnfmGenericMessage(virtualNetworkFunctionRecord,
      // Action.GRANT_OPERATION);
      return nfvMessage;
    } else {
      //Save the vnfr since in the grantLifecycleOperation method we use vdu.getId()
      setHistoryLifecycleEvent(new Date());
      saveVirtualNetworkFunctionRecord();
      Map<String, VimInstance> vimInstancesChosen =
          lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord);
      log.debug("VimInstances chosen are: " + vimInstancesChosen);
      log.debug(vimInstancesChosen.size() + " == " + virtualNetworkFunctionRecord.getVdu().size());
      if (vimInstancesChosen.size() == virtualNetworkFunctionRecord.getVdu().size()) {
        log.info(
            "Finished task: GrantOperation on VNFR: " + virtualNetworkFunctionRecord.getName());

        saveVirtualNetworkFunctionRecord();
        log.debug("Hibernate version is: " + virtualNetworkFunctionRecord.getHb_version());
        OrVnfmGrantLifecycleOperationMessage nfvMessage =
            new OrVnfmGrantLifecycleOperationMessage();
        nfvMessage.setGrantAllowed(true);
        nfvMessage.setVduVim(vimInstancesChosen);
        nfvMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
        //                OrVnfmGenericMessage nfvMessage = new OrVnfmGenericMessage(virtualNetworkFunctionRecord,
        // Action.GRANT_OPERATION);
        return nfvMessage;
      } else {
        // there are not enough resources for deploying VNFR
        log.error(
            "Not enough resources for deploying VirtualNetworkFunctionRecord "
                + virtualNetworkFunctionRecord.getName());
        virtualNetworkFunctionRecord.setStatus(Status.ERROR);
        saveVirtualNetworkFunctionRecord();
        vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
        return new OrVnfmErrorMessage(
            virtualNetworkFunctionRecord,
            "Not enough resources for deploying VirtualNetworkFunctionRecord "
                + virtualNetworkFunctionRecord.getName());
      }
    }
  }

  @Override
  public boolean isAsync() {
    return true;
  }

  @Override
  protected void setEvent() {
    event = Event.GRANTED.name();
  }

  @Override
  protected void setDescription() {
    description =
        "All the resources that are contained in this VNFR were granted to be deployed in the chosen vim(s)";
  }
}
