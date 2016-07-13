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
import org.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.openbaton.nfvo.core.interfaces.VnfPlacementManagement;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
public class ScalingTask extends AbstractTask {

  @Autowired private ResourceManagement resourceManagement;
  @Autowired private VNFLifecycleOperationGranting lifecycleOperationGranting;

  @Value("${nfvo.quota.check:true}")
  private boolean checkQuota;

  @Autowired private VnfPlacementManagement vnfPlacementManagement;

  public void setUserdata(String userdata) {
    this.userdata = userdata;
  }

  private String userdata;

  public boolean isCheckQuota() {
    return checkQuota;
  }

  public void setCheckQuota(boolean checkQuota) {
    this.checkQuota = checkQuota;
  }

  @Override
  protected NFVMessage doWork() throws Exception {

    log.debug("NFVO: SCALING");
    log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
    log.debug(
        "The VNFR: "
            + virtualNetworkFunctionRecord.getName()
            + " is in status --> "
            + virtualNetworkFunctionRecord.getStatus());

    saveVirtualNetworkFunctionRecord();

    VNFComponent componentToAdd = null;
    VirtualDeploymentUnit vdu = null;
    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
      for (VNFComponent vnfComponent : virtualDeploymentUnit.getVnfc()) {
        boolean found = false;
        for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
          if (vnfComponent.getId().equals(vnfcInstance.getVnfComponent().getId())) {
            found = true;
            break;
          }
        }
        if (!found) { // new vnfComponent!
          componentToAdd = vnfComponent;
          vdu = virtualDeploymentUnit;
          break;
        }
      }
    }

    log.info("The component to add is: " + componentToAdd);
    if (checkQuota) {
      Map<String, VimInstance> vimInstanceMap =
          lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord);
      if (vimInstanceMap.size()
          == virtualNetworkFunctionRecord.getVdu().size()) { //TODO needs to be one?
        try {
          Future<String> future =
              resourceManagement.allocate(
                  vdu,
                  virtualNetworkFunctionRecord,
                  componentToAdd,
                  vimInstanceMap.get(vdu.getId()),
                  userdata);
          log.debug("Added new component with id: " + future.get());
        } catch (ExecutionException exe) {
          try {
            Throwable realException = exe.getCause();
            if (realException instanceof VimException) throw (VimException) realException;
            if (realException instanceof VimDriverException)
              throw (VimDriverException) realException;
            throw exe;
          } catch (VimException e) {
            resourceManagement.release(vdu, e.getVnfcInstance());
            virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
            saveVirtualNetworkFunctionRecord();
            OrVnfmErrorMessage errorMessage = new OrVnfmErrorMessage();
            errorMessage.setMessage(
                "Error creating VM while scale out. " + e.getLocalizedMessage());
            errorMessage.setVnfr(virtualNetworkFunctionRecord);
            errorMessage.setAction(Action.ERROR);
            vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
            return errorMessage;
          } catch (VimDriverException e) {
            virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
            saveVirtualNetworkFunctionRecord();
            OrVnfmErrorMessage errorMessage = new OrVnfmErrorMessage();
            errorMessage.setMessage(
                "Error creating VM while scale out. " + e.getLocalizedMessage());
            errorMessage.setVnfr(virtualNetworkFunctionRecord);
            errorMessage.setAction(Action.ERROR);
            vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
            return errorMessage;
          }
        }
      } else {
        log.error("Not enough resources for scale out.");
        log.error("VNFR " + virtualNetworkFunctionRecord.getName() + " stay in status ACTIVE.");
        virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
        OrVnfmErrorMessage errorMessage = new OrVnfmErrorMessage();
        errorMessage.setMessage("Not enough resources for scale out.");
        errorMessage.setVnfr(virtualNetworkFunctionRecord);
        errorMessage.setAction(Action.ERROR);
        saveVirtualNetworkFunctionRecord();
        vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
        return errorMessage;
      }
    } else {
      log.warn(
          "Please consider turning the check quota (nfvo.quota.check in openbaton.properties) to true.");
      try {
        log.debug(
            "Added new component with id: "
                + resourceManagement
                    .allocate(
                        vdu,
                        virtualNetworkFunctionRecord,
                        componentToAdd,
                        vnfPlacementManagement.choseRandom(vdu.getVimInstanceName()),
                        userdata)
                    .get());
      } catch (VimDriverException e) {
        log.error(e.getLocalizedMessage());
        virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
        OrVnfmErrorMessage errorMessage = new OrVnfmErrorMessage();
        errorMessage.setMessage(
            "Error creating VM while scale out. Please consider enabling checkQuota ;)");
        errorMessage.setVnfr(virtualNetworkFunctionRecord);
        errorMessage.setAction(Action.ERROR);
        saveVirtualNetworkFunctionRecord();
        vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
        return errorMessage;
      } catch (VimException e) {
        log.error(e.getLocalizedMessage());
        if (e.getVnfcInstance() != null) resourceManagement.release(vdu, e.getVnfcInstance());
        virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
        OrVnfmErrorMessage errorMessage = new OrVnfmErrorMessage();
        errorMessage.setMessage(
            "Error creating VM while scale out. Please consider enabling checkQuota ;)");
        errorMessage.setVnfr(virtualNetworkFunctionRecord);
        errorMessage.setAction(Action.ERROR);
        saveVirtualNetworkFunctionRecord();
        vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
        return errorMessage;
      }
    }

    log.trace("HB_VERSION == " + virtualNetworkFunctionRecord.getHb_version());
    return new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.SCALED);
  }

  @Override
  public boolean isAsync() {
    return true;
  }
}
