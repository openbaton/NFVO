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
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmStartStopMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.core.interfaces.DependencyQueuer;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 06/08/15. */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "nfvo.start")
public class InstantiateTask extends AbstractTask {

  @Autowired private DependencyManagement dependencyManagement;

  private String ordered;
  @Autowired private DependencyQueuer dependencyQueuer;

  public String getOrdered() {
    return ordered;
  }

  public void setOrdered(String ordered) {
    this.ordered = ordered;
  }

  @Override
  protected NFVMessage doWork() throws Exception {
    log.info(
        "Start INSTANTIATE task for vnfr: "
            + virtualNetworkFunctionRecord.getName()
            + " with VNFR ID "
            + virtualNetworkFunctionRecord.getId()
            + " his nsr id father is:"
            + virtualNetworkFunctionRecord.getParent_ns_id());
    VirtualNetworkFunctionRecord existing =
        vnfrRepository.findFirstById(virtualNetworkFunctionRecord.getId());
    log.trace(
        "VNFR ("
            + virtualNetworkFunctionRecord.getId()
            + ") received with hibernate version = "
            + virtualNetworkFunctionRecord.getHb_version());
    if (existing != null) {
      log.trace(
          "VNFR ("
              + existing.getId()
              + ") existing hibernat version is = "
              + existing.getHb_version());
    }

    dependencyManagement.fillDependecyParameters(virtualNetworkFunctionRecord);
    log.debug("Filled dependency parameters of " + virtualNetworkFunctionRecord.getName());
    setHistoryLifecycleEvent(new Date());
    saveVirtualNetworkFunctionRecord();
    log.debug("Saved VNFR " + virtualNetworkFunctionRecord.getName());
    NetworkServiceRecord nsr =
        networkServiceRecordRepository.findFirstById(
            virtualNetworkFunctionRecord.getParent_ns_id());
    log.debug("The current status of the NSR is " + nsr.getStatus());
    for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
      log.trace("Current VNFRs in the database: " + vnfr.getName());
    }
    dependencyQueuer.releaseVNFR(virtualNetworkFunctionRecord.getName(), nsr);
    log.info("Calling dependency management for VNFR: " + virtualNetworkFunctionRecord.getName());
    int dep = dependencyManagement.provisionDependencies(virtualNetworkFunctionRecord);
    log.info("Found " + dep + " dependencies");
    log.trace("Is ordered execution of VNFs enabled? " + Boolean.parseBoolean(ordered));

    if (ordered != null && Boolean.parseBoolean(ordered)) {
      log.debug(
          "Ordered deployments of VNF is enabled in the openbaton.properties file, in case you want to speed up the deployment, please disable it");
      if (dep == 0) {
        virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
        saveVirtualNetworkFunctionRecord();
        boolean allVnfrInInactive =
            allVnfrInInactive(
                networkServiceRecordRepository.findFirstById(
                    virtualNetworkFunctionRecord.getParent_ns_id()));
        if (allVnfrInInactive) {
          VirtualNetworkFunctionRecord nextToCallStart =
              getNextToCallStart(virtualNetworkFunctionRecord);
          if (nextToCallStart != null) {
            vnfmManager.removeVnfrName(
                virtualNetworkFunctionRecord.getParent_ns_id(), nextToCallStart.getName());
            sendStart(nextToCallStart);
            log.debug(
                "Removed "
                    + nextToCallStart.getName()
                    + " from VNFR names: "
                    + vnfmManager
                        .getVnfrNames()
                        .get(virtualNetworkFunctionRecord.getParent_ns_id()));
          }
        } else {
          log.debug("Not calling start to next VNFR because not all VNFRs are in state INACTIVE");
        }
      } else {
        log.debug("Not calling start to next VNFR");
      }
    } else {
      if (dep == 0) {
        log.debug("Send start for " + virtualNetworkFunctionRecord.getName());
        sendStart(virtualNetworkFunctionRecord);
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
    log.info(
        "Calling START to: "
            + virtualNetworkFunctionRecord.getName()
            + " because it has 0 dependencies");
    log.trace(
        "VNFR ("
            + virtualNetworkFunctionRecord.getId()
            + ") hibernate version is = "
            + virtualNetworkFunctionRecord.getHb_version());
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

  @Override
  protected void setEvent() {
    event = Event.INSTANTIATE.name();
  }

  @Override
  protected void setDescription() {
    description =
        "The instantiate lifecycle event was executed in this VNFR, this includes GRANT_OPERATION and "
            + "ALLOCATE_RESOURCES";
  }
}
