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

package org.openbaton.nfvo.vnfm_reg;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.catalogue.nfvo.messages.*;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmExecuteScriptMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmLogMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmScalingMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmUpdateMessage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.common.internal.model.EventFinishNFVO;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VirtualLinkRecordRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.vnfm.interfaces.manager.MessageGenerator;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.openbaton.vnfm.interfaces.state.VnfStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
@Scope
@Order(value = (Ordered.LOWEST_PRECEDENCE - 10)) // in order to be the second to last
@ConfigurationProperties
public class VnfmManager
    implements org.openbaton.vnfm.interfaces.manager.VnfmManager, ApplicationEventPublisherAware {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  private ApplicationEventPublisher publisher;

  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VnfStateHandler vnfStateHandler;
  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VNFRRepository vnfrRepository;
  @Autowired private VimRepository vimInstanceRepository;
  @Autowired private MessageGenerator generator;
  @Autowired private VimManagement vimManagement;
  @Autowired private VirtualLinkRecordRepository vlrRepository;

  @Value("${nfvo.start.ordered:false}")
  private boolean ordered;

  @Value("${nfvo.networks.dedicated:false}")
  private boolean dedicatedNetworks;

  private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
    List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
    list.sort(Comparator.comparing(o -> (o.getValue())));

    Map<K, V> result = new LinkedHashMap<>();
    for (Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  private static Map<String, Map<String, Integer>> vnfrNames;

  // Keep track of the NSR (for each project) which need to be restarted after the upgrade of one of their VNF
  private static Map<String, Set<String>> nsrRestartInProgress;
  private static Map<String, Set<String>> nsrRestartStopInProgress;

  @PostConstruct
  private void init() {
    vnfrNames = new LinkedHashMap<>();
    nsrRestartInProgress = new HashMap<>();
    nsrRestartStopInProgress = new HashMap<>();
  }

  @Override
  public void deploy(
      NetworkServiceDescriptor networkServiceDescriptor,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body,
      Map<String, Set<String>> vduVimInstances,
      String monitoringIp)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {

    try {

      log.debug(
          "Parameter ordered set to "
              + ordered
              + ".Consider changing it directly into the openbaton-nfvo.properties file");
      if (ordered) {
        vnfrNames.put(networkServiceRecord.getId(), new HashMap<>());
        Map<String, Integer> vnfrNamesWeighted = vnfrNames.get(networkServiceRecord.getId());
        fillVnfrNames(networkServiceDescriptor, vnfrNamesWeighted);
        vnfrNames.put(networkServiceRecord.getId(), sortByValue(vnfrNamesWeighted));

        log.debug("VNFRs ordered by dependencies: " + vnfrNames.get(networkServiceRecord.getId()));
      }

      for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
        vnfStateHandler.handleVNF(
            networkServiceDescriptor,
            networkServiceRecord,
            body,
            vduVimInstances,
            vnfd,
            monitoringIp);
      }
    } catch (BadFormatException e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Override
  public void addVnfr(
      NetworkServiceRecord networkServiceRecord,
      VirtualNetworkFunctionDescriptor vnfd,
      DeployNSRBody body,
      Map<String, Set<String>> vduVimInstances,
      String monitoringIp)
      throws NotFoundException, InterruptedException, BadFormatException, ExecutionException {

    NetworkServiceDescriptor nsd =
        nsdRepository.findFirstByIdAndProjectId(
            networkServiceRecord.getDescriptor_reference(), networkServiceRecord.getProjectId());

    vnfStateHandler.handleVNF(nsd, networkServiceRecord, body, vduVimInstances, vnfd, monitoringIp);
  }

  private void fillVnfrNames(
      NetworkServiceDescriptor networkServiceDescriptor, Map<String, Integer> vnfrNamesWeighted) {

    log.trace("Checking NSD \n\n\n" + networkServiceDescriptor);

    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {
      String virtualNetworkFunctionDescriptorName = virtualNetworkFunctionDescriptor.getName();
      int weightForVNFR =
          getWeightForVNFR(virtualNetworkFunctionDescriptor.getName(), networkServiceDescriptor);
      vnfrNamesWeighted.put(virtualNetworkFunctionDescriptorName, weightForVNFR);
      log.debug("Set weight for " + virtualNetworkFunctionDescriptorName + " to " + weightForVNFR);
    }
  }

  private int getWeightForVNFR(
      String virtualNetworkFunctionDescriptorName,
      NetworkServiceDescriptor networkServiceDescriptor) {
    int result = 0;
    for (VNFDependency dependency : networkServiceDescriptor.getVnf_dependency()) {
      if (dependency.getTarget().equals(virtualNetworkFunctionDescriptorName)) {
        result++;
        result += getWeightForVNFR(dependency.getSource(), networkServiceDescriptor);
      }
    }

    return result;
  }

  @Override
  public synchronized void findAndSetNSRStatus(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {

    if (virtualNetworkFunctionRecord == null) {
      log.debug("The VNFR is Null!");
      return;
    }

    Status status = Status.TERMINATED;
    NetworkServiceRecord networkServiceRecord = null;

    // this while loop is necessary because the NSR and it's components might have changed before the save call
    // resulting in an OptimisticLockingFailureException
    boolean foundAndSet = false;
    while (!foundAndSet) {
      try {
        try {
          log.debug("The nsr id is: " + virtualNetworkFunctionRecord.getParent_ns_id());
          networkServiceRecord =
              nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
          log.trace("Found NSR: " + networkServiceRecord);
        } catch (Exception ignored) {
          log.error("No NSR found with id " + virtualNetworkFunctionRecord.getParent_ns_id());
          return;
        }

        try {
          log.debug("looking for NSD with id: " + networkServiceRecord.getDescriptor_reference());
          if (virtualNetworkFunctionRecord.getStatus().ordinal() != Status.TERMINATED.ordinal()
              && nsdRepository
                      .findFirstById(networkServiceRecord.getDescriptor_reference())
                      .getVnfd()
                      .size()
                  > networkServiceRecord.getVnfr().size()) {
            log.debug(
                "Not all the VNFR have been created yet, it is useless to set the NSR status.");
            return;
          }
        } catch (NullPointerException ignored) {
          if (networkServiceRecord == null) {
            log.info("The Record was already deleted by a previous task");
            vnfrRepository.delete(virtualNetworkFunctionRecord);
          } else {
            log.warn("Descriptor was already removed, calculating the status anyway...");
          }
        }

        if (networkServiceRecord == null) return;

        log.debug("Checking the status of NSR: " + networkServiceRecord.getName());

        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
          log.debug("VNFR " + vnfr.getName() + " is in state: " + vnfr.getStatus());
          if (status.ordinal() > vnfr.getStatus().ordinal()) {
            status = vnfr.getStatus();
          }
        }
        status =
            networkServiceRecord
                    .getVnfr()
                    .stream()
                    .anyMatch(vnfr -> vnfr.getStatus().ordinal() == Status.TERMINATED.ordinal())
                ? Status.TERMINATED
                : status;

        log.debug("Setting NSR status to: " + status);
        networkServiceRecord.setStatus(status);
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        networkServiceRecord.setUpdatedAt(format.format(new Date()));
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        foundAndSet = true;
      } catch (OptimisticLockingFailureException ignored) {
        log.debug(
            "OptimisticLockingFailureException during findAndSet. Don't worry we will try it again.");
        status = Status.TERMINATED;
      }
    }
    log.debug("Now the status is: " + networkServiceRecord.getStatus());
    if (status.ordinal() == Status.ACTIVE.ordinal()) {

      while (true) {
        networkServiceRecord = nsrRepository.findFirstById(networkServiceRecord.getId());
        //Check if all vnfr have been received from the vnfm
        boolean nsrFilledWithAllVnfr =
            nsdRepository
                    .findFirstById(networkServiceRecord.getDescriptor_reference())
                    .getVnfd()
                    .size()
                <= networkServiceRecord.getVnfr().size();
        if (nsrFilledWithAllVnfr) {
          if (networkServiceRecord.getTask() == null) {
            networkServiceRecord.setTask("");
          }

          try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
            if (networkServiceRecord.getTask().contains("Scaling in")) {
              networkServiceRecord.setTask("Scaled in");
              networkServiceRecord.setUpdatedAt(format.format(new Date()));
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(
                  Action.SCALE_IN, networkServiceRecord, networkServiceRecord.getProjectId());
            } else if (networkServiceRecord.getTask().contains("Scaling out")) {
              networkServiceRecord.setTask("Scaled out");
              networkServiceRecord.setUpdatedAt(format.format(new Date()));
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(
                  Action.SCALE_OUT, networkServiceRecord, networkServiceRecord.getProjectId());
            } else if (networkServiceRecord.getTask().contains("Healing")) {
              networkServiceRecord.setTask("Healed");
              networkServiceRecord.setUpdatedAt(format.format(new Date()));
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(Action.HEAL, networkServiceRecord, networkServiceRecord.getProjectId());
            } else {
              networkServiceRecord.setTask("Onboarded");
              networkServiceRecord.setUpdatedAt(format.format(new Date()));
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(
                  Action.INSTANTIATE_FINISH,
                  networkServiceRecord,
                  //virtualNetworkFunctionRecord,
                  networkServiceRecord.getProjectId());
            }
          } catch (OptimisticLockingFailureException e) {
            log.debug(
                "OptimisticLockingFailureException while setting the task of the NSR. Starting next attempt.");
          }
        } else {
          log.debug("Nsr is ACTIVE but not all vnfr have been received");
        }
        break;
      }
    } else if (status.ordinal() == Status.TERMINATED.ordinal()
        && networkServiceRecord
            .getVnfr()
            .stream()
            .allMatch(vnfr -> vnfr.getStatus().ordinal() == Status.TERMINATED.ordinal())) {
      nsrRepository.delete(networkServiceRecord);
      if (dedicatedNetworks) {
        networkServiceRecord
            .getVlr()
            .parallelStream()
            .filter(
                virtualLinkRecord ->
                    vlrRepository.findByExtId(virtualLinkRecord.getExtId()).size() == 0)
            .forEach(
                vlr -> {
                  try {
                    log.info(
                        String.format("Deleting Network: %s [%s]", vlr.getName(), vlr.getExtId()));
                    vimManagement.deleteNetwork(vlr);
                  } catch (PluginException | VimException e) {
                    e.printStackTrace();
                    log.error(String.format("Not Able to delete network %s!", vlr.getExtId()));
                  } catch (NotFoundException e) {
                    log.warn(String.format("%s. probably already deleted...", e.getMessage()));
                  }
                });
      }
      publishEvent(
          Action.RELEASE_RESOURCES_FINISH,
          networkServiceRecord,
          networkServiceRecord.getProjectId());
    }
  }

  private void publishEvent(Action action, Serializable payload, String projectId) {
    ApplicationEventNFVO event = new ApplicationEventNFVO(action, payload, projectId);
    EventNFVO eventNFVO = new EventNFVO(this);
    eventNFVO.setEventNFVO(event);
    log.trace("Publishing event: " + event);
    publisher.publishEvent(eventNFVO);
  }

  @Override
  @Async
  public Future<Void> release(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VnfmManagerEndpoint endpoint = generator.getVnfm(virtualNetworkFunctionRecord.getEndpoint());
    if (endpoint == null) {
      throw new NotFoundException(
          "VnfManager of type "
              + virtualNetworkFunctionRecord.getType()
              + " (endpoint = "
              + virtualNetworkFunctionRecord.getEndpoint()
              + ") is not registered");
    }

    OrVnfmGenericMessage orVnfmGenericMessage =
        new OrVnfmGenericMessage(virtualNetworkFunctionRecord, Action.RELEASE_RESOURCES);
    VnfmSender vnfmSender;
    try {

      vnfmSender = generator.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    vnfStateHandler.executeAction(vnfmSender.sendCommand(orVnfmGenericMessage, endpoint));
    return new AsyncResult<>(null);
  }

  @Override
  @Async
  public Future<NFVMessage> requestLog(VirtualNetworkFunctionRecord vnfr, String hostname)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VnfmManagerEndpoint endpoint = generator.getVnfm(vnfr.getEndpoint());
    if (endpoint == null)
      throw new NotFoundException(
          "VnfManager of type "
              + vnfr.getType()
              + " (endpoint = "
              + vnfr.getEndpoint()
              + ") is not registered");

    OrVnfmLogMessage orVnfmLogMessage = new OrVnfmLogMessage(vnfr.getName(), hostname);
    VnfmSender vnfmSender;
    try {
      vnfmSender = generator.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }
    Future<NFVMessage> answerFuture = vnfmSender.sendCommand(orVnfmLogMessage, endpoint);
    answerFuture.get();
    NFVMessage message = answerFuture.get();
    return new AsyncResult<>(message);
  }

  @Override
  @Async
  public Future<Void> addVnfc(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent component,
      VNFRecordDependency dependency,
      String mode,
      List<String> vimInstanceNames)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VnfmManagerEndpoint endpoint = generator.getVnfm(virtualNetworkFunctionRecord.getEndpoint());

    if (endpoint == null) {
      throw new NotFoundException(
          "VnfManager of type "
              + virtualNetworkFunctionRecord.getType()
              + " (endpoint = "
              + virtualNetworkFunctionRecord.getEndpoint()
              + ") is not registered");
    }

    OrVnfmScalingMessage message = new OrVnfmScalingMessage();
    message.setAction(Action.SCALE_OUT);
    message.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
    message.setVnfPackage(
        vnfPackageRepository.findFirstById(virtualNetworkFunctionRecord.getPackageId()));
    message.setComponent(component);
    message.setExtension(generator.getExtension());
    message.setDependency(dependency);
    message.setMode(mode);

    String az = null;
    String vimInstanceName = vimInstanceNames.get(new Random().nextInt(vimInstanceNames.size()));
    if (vimInstanceName.contains(":")) {
      String[] split = vimInstanceName.split(Pattern.quote(":"));
      vimInstanceName = split[0];
      az = split[1];
    }
    BaseVimInstance vimInstance =
        vimInstanceRepository.findByProjectIdAndName(
            virtualNetworkFunctionRecord.getProjectId(), vimInstanceName);
    if (vimInstance.getMetadata() == null) vimInstance.setMetadata(new HashMap<>());
    if (az != null) vimInstance.getMetadata().put("az", az);
    message.setVimInstance(vimInstance);

    log.debug("SCALE_OUT MESSAGE IS: \n" + message);

    VnfmSender vnfmSender;
    try {

      vnfmSender = generator.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    vnfStateHandler.executeAction(vnfmSender.sendCommand(message, endpoint));
    return new AsyncResult<>(null);
  }

  @Override
  public void restartVnfr(VirtualNetworkFunctionRecord vnfr)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VnfmManagerEndpoint endpoint = generator.getVnfm(vnfr.getEndpoint());
    if (endpoint == null) {
      throw new NotFoundException(
          "VnfManager of type "
              + vnfr.getType()
              + " (endpoint = "
              + vnfr.getEndpoint()
              + ") is not registered");
    }

    VirtualNetworkFunctionDescriptor vnfd =
        vnfdRepository.findFirstByIdAndProjectId(
            vnfr.getDescriptor_reference(), vnfr.getProjectId());

    Map<String, Set<String>> vduVimInstances = new HashMap<>();
    for (VirtualDeploymentUnit vdu : vnfd.getVdu())
      vduVimInstances.put(vdu.getId(), vdu.getVimInstanceName());

    NetworkServiceRecord nsr =
        nsrRepository.findFirstByIdAndProjectId(vnfr.getParent_ns_id(), vnfr.getProjectId());

    VnfmSender vnfmSender = generator.getVnfmSender(vnfd);
    OrVnfmInstantiateMessage message =
        generator.getNextMessage(vnfd, vduVimInstances, nsr, null, null);
    // Set the vnfr in the message, so the allocate resources will be skipped
    message.setVnfr(vnfr);
    log.debug("----------Executing ACTION: " + message.getAction());

    vnfStateHandler.executeAction(vnfmSender.sendCommand(message, endpoint));
    log.info("Sent " + message.getAction() + " to VNF: " + vnfd.getName());
  }

  @Override
  @Async
  public Future<Void> removeVnfcDependency(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VnfmManagerEndpoint endpoint = generator.getVnfm(virtualNetworkFunctionRecord.getEndpoint());
    if (endpoint == null) {
      throw new NotFoundException(
          "VnfManager of type "
              + virtualNetworkFunctionRecord.getType()
              + " (endpoint = "
              + virtualNetworkFunctionRecord.getEndpoint()
              + ") is not registered");
    }

    OrVnfmScalingMessage message = new OrVnfmScalingMessage();

    message.setAction(Action.SCALE_IN);
    message.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
    message.setVnfcInstance(vnfcInstance);
    VnfmSender vnfmSender;
    try {

      vnfmSender = generator.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    vnfStateHandler.executeAction(vnfmSender.sendCommand(message, endpoint));
    return new AsyncResult<>(null);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }

  /*@Override
  public synchronized void onApplicationEvent(EventFinishNFVO event) {
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
            event.getEventNFVO().getVirtualNetworkFunctionRecord();
    publishEvent(
            event.getEventNFVO().getAction(),
            virtualNetworkFunctionRecord,
            virtualNetworkFunctionRecord.getProjectId());
    if ((event.getEventNFVO().getAction().ordinal() != Action.ALLOCATE_RESOURCES.ordinal())
            && (event.getEventNFVO().getAction().ordinal() != Action.GRANT_OPERATION.ordinal())) {
      findAndSetNSRStatus(virtualNetworkFunctionRecord);
    }

    if (event.getEventNFVO().getAction().ordinal() == Action.START.ordinal()) {
      log.info("Starting the restart of the NSR : " + virtualNetworkFunctionRecord.getParent_ns_id());
    }
  }*/

  @EventListener
  public void handleEventFinishNFVO(EventFinishNFVO eventFinishNFVO) {
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        eventFinishNFVO.getEventNFVO().getVirtualNetworkFunctionRecord();
    publishEvent(
        eventFinishNFVO.getEventNFVO().getAction(),
        virtualNetworkFunctionRecord,
        virtualNetworkFunctionRecord.getProjectId());
    if ((eventFinishNFVO.getEventNFVO().getAction().ordinal()
            != Action.ALLOCATE_RESOURCES.ordinal())
        && (eventFinishNFVO.getEventNFVO().getAction().ordinal()
            != Action.GRANT_OPERATION.ordinal())) {
      findAndSetNSRStatus(virtualNetworkFunctionRecord);
    }
  }

  @EventListener
  public void handleEventNFVO(EventNFVO eventNFVO) {
    ApplicationEventNFVO applicationEventNFVO = eventNFVO.getEventNFVO();

    if (!(applicationEventNFVO.getPayload() instanceof VirtualNetworkFunctionRecord)) return;

    VirtualNetworkFunctionRecord vnfr =
        (VirtualNetworkFunctionRecord) applicationEventNFVO.getPayload();
    String projectId = vnfr.getProjectId();
    String nsrId = vnfr.getParent_ns_id();

    if (isNsrRestartRequired(projectId, nsrId)) {
      NetworkServiceRecord nsr = nsrRepository.findFirstByIdAndProjectId(nsrId, projectId);

      // If a VNF upgrade is in progress then the NSR will be in ACTIVE only after the START of the upgraded VNF
      if (nsr.getStatus().ordinal() == Status.ACTIVE.ordinal()) {
        // To avoid sending multiple STOP messages to the same NSR in restart
        if (isNsrRestartStopRequired(projectId, nsrId)) {
          try {
            for (VirtualNetworkFunctionRecord vnfrInNsr : nsr.getVnfr()) {
              log.debug(
                  "Upgrading VNFR: "
                      + vnfr.getId()
                      + " - Restarting NSR: "
                      + nsrId
                      + " - Sending STOP to VNFR: "
                      + vnfrInNsr.getName()
                      + " (ID: "
                      + vnfrInNsr.getId()
                      + ")");
              OrVnfmStartStopMessage orVnfmStopMessage =
                  new OrVnfmStartStopMessage(vnfrInNsr, null, Action.STOP);
              //orVnfmStopMessage.setRestart(true);
              vnfStateHandler.sendMessageToVNFR(vnfrInNsr, orVnfmStopMessage);
            }
            unsetNsrRestartStopRequired(projectId, nsrId);
          } catch (NullPointerException
              | NotFoundException
              | BadFormatException
              | ExecutionException
              | InterruptedException e) {
            log.error(
                "Impossible to restart the NSR: "
                    + nsrId
                    + " after the upgrade of one of its VNF.");
            e.printStackTrace();
          }
        }
      } else if (applicationEventNFVO.getAction().ordinal() == Action.STOP.ordinal()) {
        boolean allNsrIsStopped = true;
        for (VirtualNetworkFunctionRecord vnfrInNsr : nsr.getVnfr()) {
          // If a VNFR is stopped its status is set to INACTIVE
          if (vnfrInNsr.getStatus().ordinal() != Status.INACTIVE.ordinal()) {
            allNsrIsStopped = false;
          }
        }
        // When the full NSR has been stopped then a START has to be sent to all VNFR of the NSR
        if (allNsrIsStopped) {
          try {
            for (String vnfrName : vnfrNames.get(nsrId).keySet()) {
              for (VirtualNetworkFunctionRecord vnfrInNsr : nsr.getVnfr()) {
                if (vnfrInNsr.getName().equals(vnfrName)) {
                  log.debug(
                      "Upgrading VNFR: "
                          + vnfr.getId()
                          + " - Restarting NSR: "
                          + nsrId
                          + " - Sending START to VNFR: "
                          + vnfrInNsr.getName()
                          + " (ID: "
                          + vnfrInNsr.getId()
                          + ")");
                  OrVnfmStartStopMessage orVnfmStartMessage =
                      new OrVnfmStartStopMessage(vnfrInNsr, null, Action.START);
                  //orVnfmStartMessage.setRestart(true);
                  vnfStateHandler.sendMessageToVNFR(vnfrInNsr, orVnfmStartMessage);
                }
              }
            }
          } catch (NullPointerException
              | NotFoundException
              | BadFormatException
              | ExecutionException
              | InterruptedException e) {
            log.error(
                "Impossible to restart the NSR: "
                    + nsrId
                    + " after the upgrade of one of its VNF.");
            e.printStackTrace();
          }

          unsetNsrRestartRequired(projectId, nsrId);
        }
      }
    }
  }

  //@Override
  //  public synchronized void onApplicationEvent(ApplicationEvent event) {
  //    if (event instanceof EventFinishNFVO) {
  //      EventFinishNFVO eventFinishNFVO = (EventFinishNFVO) event;
  //      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
  //          eventFinishNFVO.getEventNFVO().getVirtualNetworkFunctionRecord();
  //      publishEvent(
  //          eventFinishNFVO.getEventNFVO().getAction(),
  //          virtualNetworkFunctionRecord,
  //          virtualNetworkFunctionRecord.getProjectId());
  //      if ((eventFinishNFVO.getEventNFVO().getAction().ordinal()
  //              != Action.ALLOCATE_RESOURCES.ordinal())
  //          && (eventFinishNFVO.getEventNFVO().getAction().ordinal()
  //              != Action.GRANT_OPERATION.ordinal())) {
  //        findAndSetNSRStatus(virtualNetworkFunctionRecord);
  //      }
  //    } else if (event instanceof EventNFVO) {
  //      EventNFVO eventNFVO = (EventNFVO) event;
  //      ApplicationEventNFVO applicationEventNFVO = eventNFVO.getEventNFVO();
  //
  //      VirtualNetworkFunctionRecord vnfr =
  //          (VirtualNetworkFunctionRecord) applicationEventNFVO.getPayload();
  //      String projectId = vnfr.getProjectId();
  //      String nsrId = vnfr.getParent_ns_id();
  //
  //      if (isNsrRestartRequired(projectId, nsrId)) {
  //        NetworkServiceRecord nsr = nsrRepository.findFirstByIdAndProjectId(nsrId, projectId);
  //
  //        // If a VNF upgrade is in progress then the NSR will be in ACTIVE only after the START of the upgraded VNF
  //        if (nsr.getStatus().ordinal() == Status.ACTIVE.ordinal()) {
  //          // To avoid sending multiple STOP messages to the same NSR in restart
  //          if (isNsrRestartStopRequired(projectId, nsrId)) {
  //            try {
  //              for (VirtualNetworkFunctionRecord vnfrInNsr : nsr.getVnfr()) {
  //                log.debug(
  //                    "Upgrading VNFR: "
  //                        + vnfr.getId()
  //                        + " - Restarting NSR: "
  //                        + nsrId
  //                        + " - Sending STOP to VNFR: "
  //                        + vnfrInNsr.getName()
  //                        + " (ID: "
  //                        + vnfrInNsr.getId()
  //                        + ")");
  //                OrVnfmStartStopMessage orVnfmStopMessage =
  //                    new OrVnfmStartStopMessage(vnfrInNsr, null, Action.STOP);
  //                //orVnfmStopMessage.setRestart(true);
  //                vnfStateHandler.sendMessageToVNFR(vnfrInNsr, orVnfmStopMessage);
  //              }
  //              unsetNsrRestartStopRequired(projectId, nsrId);
  //            } catch (NullPointerException
  //                | NotFoundException
  //                | BadFormatException
  //                | ExecutionException
  //                | InterruptedException e) {
  //              log.error(
  //                  "Impossible to restart the NSR: "
  //                      + nsrId
  //                      + " after the upgrade of one of its VNF.");
  //              e.printStackTrace();
  //            }
  //          }
  //        } else if (applicationEventNFVO.getAction().ordinal() == Action.STOP.ordinal()) {
  //          boolean allNsrIsStopped = true;
  //          for (VirtualNetworkFunctionRecord vnfrInNsr : nsr.getVnfr()) {
  //            // If a VNFR is stopped its status is set to INACTIVE
  //            if (vnfrInNsr.getStatus().ordinal() != Status.INACTIVE.ordinal()) {
  //              allNsrIsStopped = false;
  //            }
  //          }
  //          // When the full NSR has been stopped then a START has to be sent to all VNFR of the NSR
  //          if (allNsrIsStopped) {
  //            try {
  //              for (String vnfrName : vnfrNames.get(nsrId).keySet()) {
  //                for (VirtualNetworkFunctionRecord vnfrInNsr : nsr.getVnfr()) {
  //                  if (vnfrInNsr.getName().equals(vnfrName)) {
  //                    log.debug(
  //                        "Upgrading VNFR: "
  //                            + vnfr.getId()
  //                            + " - Restarting NSR: "
  //                            + nsrId
  //                            + " - Sending START to VNFR: "
  //                            + vnfrInNsr.getName()
  //                            + " (ID: "
  //                            + vnfrInNsr.getId()
  //                            + ")");
  //                    OrVnfmStartStopMessage orVnfmStartMessage =
  //                        new OrVnfmStartStopMessage(vnfrInNsr, null, Action.START);
  //                    //orVnfmStartMessage.setRestart(true);
  //                    vnfStateHandler.sendMessageToVNFR(vnfrInNsr, orVnfmStartMessage);
  //                  }
  //                }
  //              }
  //            } catch (NullPointerException
  //                | NotFoundException
  //                | BadFormatException
  //                | ExecutionException
  //                | InterruptedException e) {
  //              log.error(
  //                  "Impossible to restart the NSR: "
  //                      + nsrId
  //                      + " after the upgrade of one of its VNF.");
  //              e.printStackTrace();
  //            }
  //
  //            unsetNsrRestartRequired(projectId, nsrId);
  //          }
  //        }
  //      }
  //    }
  //  }

  @Override
  public Map<String, Map<String, Integer>> getVnfrNames() {
    return vnfrNames;
  }

  @Override
  public void removeVnfrName(String nsdId, String vnfrName) {
    vnfrNames.get(nsdId).remove(vnfrName);
    if (vnfrNames.get(nsdId).isEmpty()) {
      vnfrNames.remove(nsdId);
    }
  }

  @Override
  public void updateScript(Script script, String vnfPackageId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {

    for (VirtualNetworkFunctionDescriptor vnfd : vnfdRepository.findAll()) {
      if (vnfd.getVnfPackageLocation() != null
          && vnfd.getVnfPackageLocation().equals(vnfPackageId)) {
        for (VirtualNetworkFunctionRecord vnfr : vnfrRepository.findAll()) {
          OrVnfmUpdateMessage orVnfmUpdateMessage = new OrVnfmUpdateMessage();
          orVnfmUpdateMessage.setScript(script);
          orVnfmUpdateMessage.setVnfr(vnfr);
          if (vnfr.getPackageId().equals(vnfPackageId)) {
            vnfStateHandler.sendMessageToVNFR(vnfr, orVnfmUpdateMessage);
          }
        }
      }
    }
  }

  @Override
  public void executeScript(String vnfrId, Script script)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VirtualNetworkFunctionRecord vnfr = vnfrRepository.findFirstById(vnfrId);
    OrVnfmExecuteScriptMessage orVnfmExecuteScriptMessage = new OrVnfmExecuteScriptMessage();
    orVnfmExecuteScriptMessage.setScript(script);
    orVnfmExecuteScriptMessage.setVnfr(vnfr);

    vnfStateHandler.sendMessageToVNFR(vnfr, orVnfmExecuteScriptMessage);
  }

  @Override
  public void updateVnfr(String nsrId, String vnfrId, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VirtualNetworkFunctionRecord vnfr =
        vnfrRepository.findByIdAndParent_ns_idAndProjectId(vnfrId, nsrId, projectId);
    OrVnfmUpdateMessage orVnfmUpdateMessage = new OrVnfmUpdateMessage();
    orVnfmUpdateMessage.setScript(null);
    orVnfmUpdateMessage.setVnfr(vnfr);
    vnfStateHandler.sendMessageToVNFR(vnfr, orVnfmUpdateMessage);
  }

  /**
   * Trigger the restart of the NSR after the upgrade of the VNFR
   *
   * @param nsrId
   * @param vnfrId
   * @param projectId
   * @throws NotFoundException
   * @throws BadFormatException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Override
  public void upgradeVnfr(String nsrId, String vnfrId, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    NetworkServiceRecord nsr = nsrRepository.findFirstByIdAndProjectId(nsrId, projectId);
    NetworkServiceDescriptor nsd = nsdRepository.findFirstById(nsr.getDescriptor_reference());

    setNsrRestartRequired(projectId, nsrId);

    // Create ordered list of VNFR to be restarted
    vnfrNames.put(nsrId, new HashMap<>());
    Map<String, Integer> vnfrNamesWeighted = vnfrNames.get(nsrId);
    fillVnfrNames(nsd, vnfrNamesWeighted);
    vnfrNames.put(nsrId, sortByValue(vnfrNamesWeighted));

    log.debug("VNFRs ordered by dependencies: " + vnfrNames.get(nsrId));
  }

  private boolean isNsrRestartRequired(String projectId, String nsrId) {
    if (nsrRestartInProgress.isEmpty()) return false;
    return nsrRestartInProgress.get(projectId).contains(nsrId);
  }

  private boolean isNsrRestartStopRequired(String projectId, String nsrId) {
    if (nsrRestartStopInProgress.isEmpty()) return false;
    return nsrRestartStopInProgress.get(projectId).contains(nsrId);
  }

  private void setNsrRestartRequired(String projectId, String nsrId) {
    nsrRestartInProgress.putIfAbsent(projectId, new HashSet<>());
    nsrRestartInProgress.get(projectId).add(nsrId);
    nsrRestartStopInProgress.putIfAbsent(projectId, new HashSet<>());
    nsrRestartStopInProgress.get(projectId).add(nsrId);
  }

  private void unsetNsrRestartRequired(String projectId, String nsrId) {
    if (nsrRestartInProgress.get(projectId) != null)
      nsrRestartInProgress.get(projectId).remove(nsrId);
  }

  public void unsetNsrRestartStopRequired(String projectId, String nsrId) {
    if (nsrRestartStopInProgress.get(projectId) != null)
      nsrRestartStopInProgress.get(projectId).remove(nsrId);
  }
}
