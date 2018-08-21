/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.core.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrLogMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Created by lto on 17/05/16. */
@Service
public class LogManagement implements org.openbaton.nfvo.core.interfaces.LogManagement {

  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;
  @Autowired private VnfmManager vnfmManager;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public VnfmOrLogMessage getLog(String nsrId, String vnfrName, String hostname)
      throws NotFoundException, InterruptedException, BadFormatException, ExecutionException {
    for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord :
        networkServiceRecordRepository.findFirstById(nsrId).getVnfr()) {
      if (virtualNetworkFunctionRecord.getName().equals(vnfrName)) {
        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
          for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
            if (hostname.equals(vnfcInstance.getHostname())) {

              log.debug("Requesting log from VNFM");
              Future<NFVMessage> futureMessage =
                  vnfmManager.requestLog(virtualNetworkFunctionRecord, hostname);
              VnfmOrLogMessage vnfmOrLogMessage = (VnfmOrLogMessage) futureMessage.get();
              return vnfmOrLogMessage;
            }
          }
        }
      }
    }

    throw new NotFoundException("Error something was not found");
  }
}
