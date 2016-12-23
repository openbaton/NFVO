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

import java.util.Date;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 06/08/15. */
@Service
@Scope("prototype")
public class ReleaseresourcesTask extends AbstractTask {

  @Autowired private ResourceManagement resourceManagement;

  @Override
  protected NFVMessage doWork() throws Exception {
    log.info("Release resources for VNFR: " + virtualNetworkFunctionRecord.getName());

    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
      log.debug(
          "Removing VDU: "
              + virtualDeploymentUnit.getHostname()
              + " from VNFR "
              + virtualNetworkFunctionRecord.getId());
      for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
        log.debug(
            "Removing VNFC instance: "
                + vnfcInstance
                + " from VNFR "
                + virtualNetworkFunctionRecord.getId());
        this.resourceManagement.release(virtualDeploymentUnit, vnfcInstance);
      }
    }
    setHistoryLifecycleEvent(new Date());
    saveVirtualNetworkFunctionRecord();
    return null;
  }

  @Override
  public boolean isAsync() {
    return true;
  }

  @Override
  protected void setEvent() {
    event = Event.RELEASE.name();
  }

  @Override
  protected void setDescription() {
    description = "The resources of this VNFR were correctly released";
  }
}
