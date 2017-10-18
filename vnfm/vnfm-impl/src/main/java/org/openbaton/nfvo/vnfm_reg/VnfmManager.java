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

package org.openbaton.nfvo.vnfm_reg;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmLogMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmScalingMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmUpdateMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.internal.model.EventFinishNFVO;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.repositories.VimRepository;
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
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/** Created by lto on 08/07/15. */
@Service
@Scope
@Order(value = (Ordered.LOWEST_PRECEDENCE - 10)) // in order to be the second to last
@ConfigurationProperties
public class VnfmManager
    implements org.openbaton.vnfm.interfaces.manager.VnfmManager,
        ApplicationEventPublisherAware,
        ApplicationListener<EventFinishNFVO> {

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

  @Value("${nfvo.start.ordered:false}")
  private boolean ordered;

  private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
    List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
    Collections.sort(
        list,
        new Comparator<Entry<K, V>>() {
          @Override
          public int compare(Entry<K, V> o1, Entry<K, V> o2) {
            return (o1.getValue()).compareTo(o2.getValue());
          }
        });

    Map<K, V> result = new LinkedHashMap<>();
    for (Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  private static Map<String, Map<String, Integer>> vnfrNames;

  public boolean getOrdered() {
    return ordered;
  }

  public void setOrdered(boolean ordered) {
    this.ordered = ordered;
  }

  @PostConstruct
  private void init() {
    vnfrNames = new LinkedHashMap<>();
  }

  @Override
  @Async
  public Future<Void> deploy(
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
              + ".Consider changing it directly into the openbaton.properties file");
      if (ordered) {
        vnfrNames.put(networkServiceRecord.getId(), new HashMap<String, Integer>());
        Map<String, Integer> vnfrNamesWeighted = vnfrNames.get(networkServiceRecord.getId());
        fillVnfrNames(networkServiceDescriptor, vnfrNamesWeighted);
        vnfrNames.put(networkServiceRecord.getId(), sortByValue(vnfrNamesWeighted));

        log.debug("VNFRs ordered by dependencies: " + vnfrNames.get(networkServiceRecord.getId()));
      }

      for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
        vnfStateHandler.handleVNF(
            networkServiceDescriptor, networkServiceRecord, body, vduVimInstances, vnfd);
      }

      return new AsyncResult<>(null);
    } catch (BadFormatException e) {
      e.printStackTrace();
      throw e;
    }
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
                  != networkServiceRecord.getVnfr().size()) {
            log.debug(
                "Not all the VNFR have been created yet, it is useless to set the NSR status.");
            return;
          }
        } catch (NullPointerException ignored) {
          if (networkServiceRecord == null) {
            log.info("The Record was already deleted by a previous task");
          } else {
            log.warn("Descriptor was already removed, calculating the status anyway...");
          }
        }

        log.debug("Checking the status of NSR: " + networkServiceRecord.getName());

        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
          log.debug("VNFR " + vnfr.getName() + " is in state: " + vnfr.getStatus());
          if (status.ordinal() > vnfr.getStatus().ordinal()) {
            status = vnfr.getStatus();
          }
        }

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

      boolean savedNsr;
      do {
        savedNsr = true;
        networkServiceRecord = nsrRepository.findFirstById(networkServiceRecord.getId());
        //Check if all vnfr have been received from the vnfm
        boolean nsrFilledWithAllVnfr =
            nsdRepository
                    .findFirstById(networkServiceRecord.getDescriptor_reference())
                    .getVnfd()
                    .size()
                == networkServiceRecord.getVnfr().size();
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
                  networkServiceRecord.getProjectId());
            }
          } catch (OptimisticLockingFailureException e) {
            log.error(
                "FFFF OptimisticLockingFailureException while setting the task of the NSR. Don't worry we will try it"
                    + " again.");
            savedNsr = false;
          }
        } else {
          log.debug("Nsr is ACTIVE but not all vnfr have been received");
        }
      } while (!savedNsr);
    } else if (status.ordinal() == Status.TERMINATED.ordinal()) {
      publishEvent(
          Action.RELEASE_RESOURCES_FINISH,
          networkServiceRecord,
          networkServiceRecord.getProjectId());
      nsrRepository.delete(networkServiceRecord);
    }

    log.trace("Thread: " + Thread.currentThread().getId() + " finished findAndSet");
  }

  private NetworkServiceRecord safeSaveNetworkServiceRecord(
      NetworkServiceRecord networkServiceRecord) {
    boolean foundAndSet = false;

    while (!foundAndSet) {
      try {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        networkServiceRecord.setUpdatedAt(format.format(new Date()));
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        foundAndSet = true;
      } catch (OptimisticLockingFailureException ignored) {
        log.debug(
            "OptimisticLockingFailureException during findAndSet. Don't worry we will try it again.");
      }
    }
    return networkServiceRecord;
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

    message.setVimInstance(
        vimInstanceRepository.findByProjectIdAndName(
            virtualNetworkFunctionRecord.getProjectId(),
            vimInstanceNames.get(new Random().nextInt(vimInstanceNames.size()))));

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

  @Override
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
  }

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
}
