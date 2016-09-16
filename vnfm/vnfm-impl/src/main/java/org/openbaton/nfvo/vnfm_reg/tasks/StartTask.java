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

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmStartStopMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.VNFCInstanceRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "nfvo.start")
public class StartTask extends AbstractTask {

  @Autowired private VNFCInstanceRepository vnfcInstanceRepository;

  private String ordered;
  private VNFCInstance vnfcInstance;

  @Override
  public boolean isAsync() {
    return true;
  }

  public void setOrdered(String ordered) {
    this.ordered = ordered;
  }

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }

  @Override
  public NFVMessage doWork() throws Exception {
    log.info("Started VNFR: " + virtualNetworkFunctionRecord.getName());
    VirtualNetworkFunctionRecord existing =
        vnfrRepository.findFirstById(virtualNetworkFunctionRecord.getId());
    log.trace("vnfr arrived version= " + virtualNetworkFunctionRecord.getHb_version());
    log.trace("vnfr existing version= " + existing.getHb_version());

    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
      for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
        log.trace("VNFCI arrived version: " + vnfcInstance.getVersion());
      }
    }

    for (VirtualDeploymentUnit virtualDeploymentUnit : existing.getVdu()) {
      for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
        log.trace("VNFCI existing version: " + vnfcInstance.getVersion());
      }
    }

    saveVirtualNetworkFunctionRecord();

    if (ordered != null && Boolean.parseBoolean(ordered)) {
      VirtualNetworkFunctionRecord nextToCallStart =
          getNextToCallStart(virtualNetworkFunctionRecord);
      if (nextToCallStart != null) {
        log.info(
            "Calling START to: " + nextToCallStart.getName() + " because is the next in order");
        vnfmManager.removeVnfrName(
            virtualNetworkFunctionRecord.getParent_ns_id(), nextToCallStart.getName());
        sendStart(nextToCallStart);
      }
    }

    return null;
  }

  private void sendStart(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws NotFoundException {
    VnfmSender vnfmSender;
    vnfmSender =
        this.getVnfmSender(
            vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());
    /*vnfmSender.sendCommand(
    new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.START),
    vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));*/
    vnfmSender.sendCommand(
        new OrVnfmStartStopMessage(virtualNetworkFunctionRecord, null, Action.START),
        vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
  }
}
