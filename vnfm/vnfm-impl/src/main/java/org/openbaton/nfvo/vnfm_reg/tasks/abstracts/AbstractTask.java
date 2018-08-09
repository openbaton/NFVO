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

package org.openbaton.nfvo.vnfm_reg.tasks.abstracts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.HistoryLifecycleEvent;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.openbaton.catalogue.util.EventFinishEvent;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.NsrNotFoundException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.common.internal.model.EventFinishNFVO;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.nfvo.vnfm_reg.tasks.ScaledTask;
import org.openbaton.nfvo.vnfm_reg.tasks.StartTask;
import org.openbaton.nfvo.vnfm_reg.tasks.StopTask;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.openbaton.vnfm.interfaces.state.VnfStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Putting these annotations only here won't work. */
@Service
@Scope("prototype")
public abstract class AbstractTask implements org.openbaton.vnfm.interfaces.tasks.AbstractTask {

  protected Logger log = LoggerFactory.getLogger(AbstractTask.class);

  private Action action;
  private static ReentrantLock lock = new ReentrantLock();
  private ApplicationEventPublisher publisher;

  protected abstract void setEvent();

  protected VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

  protected abstract void setDescription();

  protected String event;
  protected String description;

  @Autowired
  @Qualifier("vnfmRegister")
  protected VnfmRegister vnfmRegister;

  @Autowired private ConfigurableApplicationContext context;
  @Autowired protected VnfStateHandler vnfStateHandler;
  @Autowired protected VNFRRepository vnfrRepository;
  @Autowired protected VnfmManager vnfmManager;
  @Autowired protected NetworkServiceRecordRepository networkServiceRecordRepository;

  protected void saveVirtualNetworkFunctionRecord() throws NsrNotFoundException {

    if (networkServiceRecordRepository.exists(virtualNetworkFunctionRecord.getParent_ns_id())) {
      try {
        lock.lock();
        log.trace(
            "ACTION is: "
                + action
                + " and the VNFR id is: "
                + virtualNetworkFunctionRecord.getId());
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        if (virtualNetworkFunctionRecord.getId() == null
            || virtualNetworkFunctionRecord.getId().isEmpty()) {
          virtualNetworkFunctionRecord.setCreatedAt(format.format(new Date()));
          virtualNetworkFunctionRecord.setUpdatedAt(format.format(new Date()));
          virtualNetworkFunctionRecord =
              networkServiceRecordRepository.addVnfr(
                  virtualNetworkFunctionRecord, virtualNetworkFunctionRecord.getParent_ns_id());
        } else {
          virtualNetworkFunctionRecord.setUpdatedAt(format.format(new Date()));
          virtualNetworkFunctionRecord = vnfrRepository.save(virtualNetworkFunctionRecord);
        }
      } finally {
        lock.unlock();
      }
    }
  }

  @Override
  public Action getAction() {
    return action;
  }

  @Override
  public void setAction(Action action) {
    this.action = action;
  }

  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
    return virtualNetworkFunctionRecord;
  }

  @Override
  public void setVirtualNetworkFunctionRecord(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
  }

  @Override
  public NFVMessage call() {

    changeStatus();
    NFVMessage result = null;
    try {
      setDescription();
      setEvent();
      result = this.doWork();
    } catch (NsrNotFoundException e) {
      log.warn(e.getLocalizedMessage());
    } catch (VimException e) {
      return handleVimException(e);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof VimException) {
        return handleVimException((VimException) e.getCause());
      } else if (e.getCause() instanceof VimDriverException) {
        return handleVimDriverException((VimDriverException) e.getCause());
      } else {
        genericExceptionHandling(e);
      }
    } catch (Exception e) {
      genericExceptionHandling(e);
    }
    /* Send event finish */
    if (result == null) {
      if ((action.ordinal() != Action.ALLOCATE_RESOURCES.ordinal())
          && (action.ordinal() != Action.GRANT_OPERATION.ordinal())) {
        vnfmManager.findAndSetNSRStatus(virtualNetworkFunctionRecord);
      }
      ApplicationEventNFVO eventPublic =
          new ApplicationEventNFVO(
              action, virtualNetworkFunctionRecord, virtualNetworkFunctionRecord.getProjectId());
      EventNFVO eventNFVO = new EventNFVO(this);
      eventNFVO.setEventNFVO(eventPublic);
      log.debug(
          "Publishing eventNFVO for VNFR: "
              + virtualNetworkFunctionRecord.getName()
              + " and action "
              + action);
      publisher.publishEvent(eventNFVO);
      return null;
    } else {
      return result;
    }
  }

  private NFVMessage handleVimException(VimException e) {
    e.printStackTrace();
    log.error(e.getMessage());
    HistoryLifecycleEvent lifecycleEvent = new HistoryLifecycleEvent();
    lifecycleEvent.setEvent(Event.ERROR.name());
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd'-'HH:mm:ss:SSS'-'z");
    lifecycleEvent.setExecutedAt(format.format(new Date()));
    lifecycleEvent.setDescription(e.getMessage());
    VNFCInstance vnfcInstance = e.getVnfcInstance();

    if (vnfcInstance != null) {
      log.info("The VM was not correctly deployed. ExtId is: " + vnfcInstance.getVc_id());
      log.debug("Details are: " + vnfcInstance);
      for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
        if (vdu.getId().equals(e.getVirtualDeploymentUnit().getId())) {
          if (vdu.getVnfc_instance() == null) {
            vdu.setVnfc_instance(new LinkedHashSet<>());
          }
          vdu.getVnfc_instance().add(vnfcInstance);

          log.debug("Found VDU and set vnfcInstance");
        }
      }
    }
    virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
    try {
      saveVirtualNetworkFunctionRecord();
    } catch (NotFoundException e1) {
      log.error(e1.getLocalizedMessage());
    }
    return new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
  }

  private NFVMessage handleVimDriverException(VimDriverException e) {
    e.printStackTrace();
    log.error(e.getMessage());
    HistoryLifecycleEvent lifecycleEvent = new HistoryLifecycleEvent();
    lifecycleEvent.setEvent(Event.ERROR.name());
    lifecycleEvent.setDescription(e.getMessage());
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd'-'HH:mm:ss:SSS'-'z");
    lifecycleEvent.setExecutedAt(format.format(new Date()));
    if (virtualNetworkFunctionRecord.getLifecycle_event_history() == null) {
      virtualNetworkFunctionRecord.setLifecycle_event_history(new LinkedHashSet<>());
    }
    virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
    virtualNetworkFunctionRecord.setStatus(Status.ERROR);
    try {
      saveVirtualNetworkFunctionRecord();
    } catch (NotFoundException e1) {
      log.error(e1.getMessage());
    }
    return new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
  }

  private void genericExceptionHandling(Exception e) {
    e.printStackTrace();
    log.debug("The exception is: " + e.getClass().getName());

    if (e.getCause() != null) {
      log.debug("The cause is: " + e.getCause().getClass().getName());
    }

    VnfmSender vnfmSender;
    try {
      vnfmSender =
          this.getVnfmSender(
              vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()).getEndpointType());
    } catch (NotFoundException e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
    NFVMessage message = new OrVnfmErrorMessage(virtualNetworkFunctionRecord, e.getMessage());
    try {
      vnfStateHandler.executeAction(
          vnfmSender.sendCommand(
              message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint())));
    } catch (NotFoundException
        | BadFormatException
        | InterruptedException
        | ExecutionException e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
    if (log.isDebugEnabled()) {
      log.error(
          "There was an uncaught exception in task: " + virtualNetworkFunctionRecord.getTask(), e);
    } else {
      log.error(
          String.format(
              "There was an uncaught exception for VNFR %s in task %s. Message is: %s",
              virtualNetworkFunctionRecord.getName(),
              virtualNetworkFunctionRecord.getTask(),
              e.getMessage()));
    }

    EventFinishEvent eventFinishEvent = new EventFinishEvent();
    eventFinishEvent.setAction(Action.ERROR);
    virtualNetworkFunctionRecord.setStatus(Status.ERROR);
    setHistoryLifecycleEvent();
    try {
      saveVirtualNetworkFunctionRecord();
    } catch (NotFoundException e1) {
      log.error(e1.getMessage());
    }
    log.info(
        "Saved the VNFR "
            + virtualNetworkFunctionRecord.getName()
            + " with status error after an exception");
    eventFinishEvent.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
    EventFinishNFVO event = new EventFinishNFVO(this);
    event.setEventNFVO(eventFinishEvent);
    this.publisher.publishEvent(event);
  }

  protected abstract NFVMessage doWork() throws Exception;

  public boolean isAsync() {
    return true;
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }

  protected VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException {
    String senderName = endpointType.toString().toLowerCase() + "VnfmSender";
    return (VnfmSender) this.context.getBean(senderName);
  }

  private void changeStatus() {
    log.debug("Action is: " + action);
    Status status = virtualNetworkFunctionRecord.getStatus();
    log.debug("Previous status is: " + status);
    switch (action) {
      case ALLOCATE_RESOURCES:
        status = Status.NULL;
        break;
      case SCALE_IN:
        break;
      case SCALING:
        status = Status.SCALING;
        break;
      case ERROR:
        status = Status.ERROR;
        break;
      case MODIFY:
        status = Status.INACTIVE;
        break;
      case RELEASE_RESOURCES:
        status = Status.TERMINATED;
        break;
      case HEAL:
        status = Status.ACTIVE;
        break;
      case GRANT_OPERATION:
        status = Status.NULL;
        break;
      case INSTANTIATE:
        status = Status.INITIALIZED;
        break;
      case SCALED:
        status = Status.ACTIVE;
        {
          VNFCInstance vnfciScaled = ((ScaledTask) this).getVnfcInstance();
          vnfciScaled.setState("ACTIVE");
          break;
        }
      case RELEASE_RESOURCES_FINISH:
        status = Status.TERMINATED;
        break;
      case INSTANTIATE_FINISH:
        status = Status.ACTIVE;
        break;
      case CONFIGURE:
        break;
      case START:
        {
          VNFCInstance vnfciStarted = ((StartTask) this).getVnfcInstance();
          for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            for (VNFCInstance instanceInVNFR : vdu.getVnfc_instance()) {

              log.debug("VNFCInstance: " + instanceInVNFR.getHostname());

              // if vnfciStarted is not null then the START message received refers to the VNFCInstance
              if (vnfciStarted != null) {
                if (instanceInVNFR.getId().equals(vnfciStarted.getId())) {
                  instanceInVNFR.setState("ACTIVE");
                }
              } else { // START refers to the VNFR then the status of all the VNFCInstance is set to "ACTIVE"
                instanceInVNFR.setState("ACTIVE");
              }
            }
          }

          status = Status.ACTIVE;
          break;
        }
      case STOP:
        VNFCInstance vnfciStopped = ((StopTask) this).getVnfcInstance();
        boolean stopVNFR = true;

        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
          for (VNFCInstance instanceInVNFR : vdu.getVnfc_instance()) {

            // if vnfciStopped is NOT null then the STOP message received refers to the VNFCInstance
            if (vnfciStopped != null) {
              // set the status of the stopped VNFCInstance inside the VNFR to "INACTIVE"
              if (instanceInVNFR.getId().equals(vnfciStopped.getId())) {
                instanceInVNFR.setState("INACTIVE");
              }

              // check for the "last VNFCInstance being stopped": as long as in the record there is
              // at least one VNFCInstance in state "ACTIVE" then the VNFR status remains "ACTIVE"
              if (instanceInVNFR.getState().equals("ACTIVE")) {
                stopVNFR = false;
              }
            } else { // STOP refers to the VNFR then the status of all the VNFCInstance is set to "INACTIVE"
              instanceInVNFR.setState("INACTIVE");
            }
          }
        }
        if (stopVNFR) {
          status = Status.INACTIVE;
        }
        break;
    }
    virtualNetworkFunctionRecord.setStatus(status);
    log.debug(
        "Changing status of VNFR: "
            + virtualNetworkFunctionRecord.getName()
            + " ( "
            + virtualNetworkFunctionRecord.getId()
            + " ) to "
            + status);
  }

  protected VirtualNetworkFunctionRecord getNextToCallStart(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {

    Map<String, Integer> vnfrNames =
        vnfmManager.getVnfrNames().get(virtualNetworkFunctionRecord.getParent_ns_id());

    if (vnfrNames != null) {

      log.debug("List of VNFRs to start: " + vnfrNames);

      if (!vnfrNames.isEmpty()) {
        for (Entry<String, Integer> entry : vnfrNames.entrySet()) {
          vnfrNames.remove(entry.getKey());
          for (VirtualNetworkFunctionRecord vnfr :
              networkServiceRecordRepository
                  .findFirstById(virtualNetworkFunctionRecord.getParent_ns_id())
                  .getVnfr()) {
            if (vnfr.getName().equals(entry.getKey())) {
              return vnfr;
            }
          }

          return null;
        }
      }
    }
    return null;
  }

  protected boolean allVnfrInInactive(NetworkServiceRecord nsr) {
    for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : nsr.getVnfr()) {
      if (virtualNetworkFunctionRecord.getStatus().ordinal() < Status.INACTIVE.ordinal()) {
        log.trace(
            "VNFR "
                + virtualNetworkFunctionRecord.getName()
                + " is in state: "
                + virtualNetworkFunctionRecord.getStatus());
        return false;
      }
    }
    return true;
  }

  protected synchronized void setHistoryLifecycleEvent() {
    HistoryLifecycleEvent lifecycleEvent = new HistoryLifecycleEvent();
    lifecycleEvent.setEvent(event);
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd'-'HH:mm:ss:SSS'-'z");
    lifecycleEvent.setExecutedAt(format.format(new Date()));
    lifecycleEvent.setDescription(description);

    if (virtualNetworkFunctionRecord.getLifecycle_event_history() == null) {
      virtualNetworkFunctionRecord.setLifecycle_event_history(new LinkedHashSet<>());
    }
    virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
    log.debug("Added lifecycle event history: " + lifecycleEvent);
  }

  protected void printOldAndNewHibernateVersion() {
    if (virtualNetworkFunctionRecord.getId() != null
        && vnfrRepository.exists(virtualNetworkFunctionRecord.getId())) {
      VirtualNetworkFunctionRecord existing =
          vnfrRepository.findFirstById(virtualNetworkFunctionRecord.getId());

      log.trace(
          this.event
              + ": VDU ("
              + virtualNetworkFunctionRecord.getId()
              + ") received with hibernate version = "
              + virtualNetworkFunctionRecord.getHbVersion());
      log.trace(
          this.event
              + ": VDU ("
              + existing.getId()
              + ") existing hibernate version is = "
              + existing.getHbVersion());

      virtualNetworkFunctionRecord
          .getVdu()
          .forEach(
              vdu ->
                  log.trace(
                      this.event
                          + ": VDU ("
                          + vdu.getId()
                          + ") received with hibernate version = "
                          + vdu.getHbVersion()));

      existing
          .getVdu()
          .forEach(
              vdu ->
                  log.trace(
                      this.event
                          + ": VDU ("
                          + vdu.getId()
                          + ") existing hibernate version is = "
                          + vdu.getHbVersion()));
    }
  }
}
