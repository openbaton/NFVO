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
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmStartStopMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by lto on 06/08/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "nfvo.start")
public class ModifyTask extends AbstractTask {

  @Autowired private VnfmRegister vnfmRegister;

  private String ordered;

  @Override
  protected NFVMessage doWork() throws Exception {

    description = "All the configuration scripts setting up dependencies were correctly executed";
    virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
    log.info("MODIFY finished for VNFR: " + virtualNetworkFunctionRecord.getName());
    log.trace("VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
    setHistoryLifecycleEvent(new Date());
    saveVirtualNetworkFunctionRecord();
    log.trace("Now VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
    log.debug(
        "VNFR "
            + virtualNetworkFunctionRecord.getName()
            + " Status is: "
            + virtualNetworkFunctionRecord.getStatus());
    boolean allVnfrInInactive =
        allVnfrInInactive(
            networkServiceRecordRepository.findFirstById(
                virtualNetworkFunctionRecord.getParent_ns_id()));
    log.trace("Ordered string is: \"" + ordered + "\"");
    log.debug("Is ordered? " + Boolean.parseBoolean(ordered));
    log.debug("Are all VNFR in inactive? " + allVnfrInInactive);

    if (ordered != null && Boolean.parseBoolean(ordered)) {
      if (allVnfrInInactive) {
        VirtualNetworkFunctionRecord nextToCallStart =
            getNextToCallStart(virtualNetworkFunctionRecord);
        if (nextToCallStart != null) {
          vnfmManager.removeVnfrName(
              virtualNetworkFunctionRecord.getParent_ns_id(), nextToCallStart.getName());
          sendStart(nextToCallStart);
        }
        log.info("Not found next VNFR to call start");
      } else {
        log.debug(
            "After MODIFY of "
                + virtualNetworkFunctionRecord.getName()
                + ", not calling start to next VNFR because not all VNFRs are in state INACTIVE");
      }
    } else {
      sendStart(virtualNetworkFunctionRecord);
    }
    return null;
  }

  private void sendStart(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws NotFoundException {
    VnfmSender vnfmSender;
    log.info("Calling START to: " + virtualNetworkFunctionRecord.getName() + " after MODIFY");
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

  @Override
  public boolean isAsync() {
    return true;
  }

  public void setOrdered(String ordered) {
    this.ordered = ordered;
  }

  @Override
  protected void setEvent() {
    event = Event.CONFIGURE.name();
  }

  @Override
  protected void setDescription() {
    description = "The Modify task was executed and the dependencies set up";
  }
}
