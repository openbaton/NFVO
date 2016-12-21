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
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 06/08/15. */
@Service
@Scope("prototype")
public class ErrorTask extends AbstractTask {

  private Exception exception;

  public void setNsrId(String nsrId) {
    this.nsrId = nsrId;
  }

  private String nsrId;

  @Override
  public boolean isAsync() {
    return true;
  }

  @Override
  protected void setEvent() {
    event = Event.ERROR.name();
  }

  @Override
  protected void setDescription() {
    if (exception != null) {
      if (exception.getMessage().length() > 1024) {
        description = exception.getMessage().substring(0, 1024);
      } else description = exception.getMessage();
    } else {
      description = "An Error Occurred in this VNFR, check the VNFM logs for more info";
    }
  }

  @Override
  public NFVMessage doWork() throws Exception {

    if (log.isDebugEnabled()) {
      log.error("ERROR from VNFM: ", this.getException());
    } else {
      log.error("ERROR from VNFM: " + this.getException().getMessage());
    }

    if (virtualNetworkFunctionRecord.getId() == null) {
      saveVirtualNetworkFunctionRecord();
    }

    if (virtualNetworkFunctionRecord != null) {
      try {
        log.trace(
            "VNFR ("
                + virtualNetworkFunctionRecord.getId()
                + ") existing hibernate version is = "
                + vnfrRepository
                    .findFirstById(virtualNetworkFunctionRecord.getId())
                    .getHb_version());
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.error(e.getMessage(), e);
        }
      }
      log.trace(
          "VNFR ("
              + virtualNetworkFunctionRecord.getId()
              + ") received hibernate version is: "
              + virtualNetworkFunctionRecord.getHb_version());
      log.error(
          "Received ERROR message from VNFM related to VNFR: "
              + virtualNetworkFunctionRecord.getName());
      virtualNetworkFunctionRecord.setStatus(Status.ERROR);
      setHistoryLifecycleEvent(new Date());
      saveVirtualNetworkFunctionRecord();
    } else {
      log.error(
          "Received Error from some VNFM. No VNFR was received, maybe the error came before the createVNFR? Check the"
              + " VNFM Logs");
      NetworkServiceRecord networkServiceRecord =
          networkServiceRecordRepository.findFirstById(nsrId);
      networkServiceRecord.setStatus(Status.ERROR);
      log.debug("Setting the NSR " + networkServiceRecord.getName() + " in state ERROR");
      networkServiceRecordRepository.save(networkServiceRecord);
    }

    return null;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public Exception getException() {
    return exception;
  }
}
