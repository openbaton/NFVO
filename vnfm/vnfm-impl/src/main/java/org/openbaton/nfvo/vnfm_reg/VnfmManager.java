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

import com.google.gson.Gson;

import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmScalingMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmUpdateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrAllocateResourcesMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrErrorMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScaledMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScalingMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrStartStopMessage;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.internal.model.EventFinishNFVO;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.AllocateresourcesTask;
import org.openbaton.nfvo.vnfm_reg.tasks.ErrorTask;
import org.openbaton.nfvo.vnfm_reg.tasks.HealTask;
import org.openbaton.nfvo.vnfm_reg.tasks.ReleaseresourcesTask;
import org.openbaton.nfvo.vnfm_reg.tasks.ScaledTask;
import org.openbaton.nfvo.vnfm_reg.tasks.ScalingTask;
import org.openbaton.nfvo.vnfm_reg.tasks.StartTask;
import org.openbaton.nfvo.vnfm_reg.tasks.StopTask;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

/**
 * Created by lto on 08/07/15.
 */
@Service
@Scope
@Order(value = (Ordered.LOWEST_PRECEDENCE - 10)) // in order to be the second to last
@ConfigurationProperties
public class VnfmManager
    implements org.openbaton.vnfm.interfaces.manager.VnfmManager, ApplicationEventPublisherAware,
        ApplicationListener<EventFinishNFVO> {

  private static Map<String, Map<String, Integer>> vnfrNames;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  @Qualifier("vnfmRegister")
  private VnfmRegister vnfmRegister;

  private ApplicationEventPublisher publisher;
  private ThreadPoolTaskExecutor asyncExecutor;
  @Autowired private ConfigurableApplicationContext context;
  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VimRepository vimInstanceRepository;
  @Autowired private Gson gson;

  @Value("${nfvo.start.ordered:false}")
  private boolean ordered;

  @Value("${nfvo.vmanager.executor.maxpoolsize:30}")
  private int maxPoolSize;

  @Value("${nfvo.vmanager.executor.corepoolsize:5}")
  private int corePoolSize;

  @Value("${nfvo.vmanager.executor.queuecapacity:100}")
  private int queueCapacity;

  @Value("${nfvo.vmanager.executor.keepalive:20}")
  private int keepAliveSeconds;

  @Value("${nfvo.rabbit.brokerIp:127.0.0.1}")
  private String brokerIp;

  @Value("${nfvo.monitoring.ip:}")
  private String monitoringIp;

  @Value("${nfvo.timezone:CET}")
  private String timezone;

  @Value("${nfvo.ems.version:0.15}")
  private String emsVersion;

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${nfvo.ems.queue.heartbeat:60}")
  private String emsHeartbeat;

  @Value("${nfvo.ems.queue.autodelete:true}")
  private String emsAutodelete;

  @Value("${spring.rabbitmq.port:5672}")
  private String brokerPort;

  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VNFRRepository vnfrRepository;

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

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getKeepAliveSeconds() {
    return keepAliveSeconds;
  }

  public void setKeepAliveSeconds(int keepAliveSeconds) {
    this.keepAliveSeconds = keepAliveSeconds;
  }

  public int getQueueCapacity() {
    return queueCapacity;
  }

  public void setQueueCapacity(int queueCapacity) {
    this.queueCapacity = queueCapacity;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public boolean getOrdered() {
    return ordered;
  }

  public void setOrdered(boolean ordered) {
    this.ordered = ordered;
  }

  @Override
  @PostConstruct
  public void init() {

    log.debug("Running VnfmManager init");

    vnfrNames = new LinkedHashMap<>();
    /**
     * Asynchronous thread executor configuration
     */
    this.asyncExecutor = new ThreadPoolTaskExecutor();

    this.asyncExecutor.setThreadNamePrefix("OpenbatonTask-");

    this.asyncExecutor.setMaxPoolSize(maxPoolSize);
    this.asyncExecutor.setCorePoolSize(corePoolSize);
    this.asyncExecutor.setQueueCapacity(queueCapacity);
    this.asyncExecutor.setKeepAliveSeconds(keepAliveSeconds);

    this.asyncExecutor.initialize();

    log.debug("AsyncExecutor is: " + asyncExecutor);

    log.trace("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    log.trace("ThreadPollTaskExecutor configuration:");
    log.trace("MaxPoolSize = " + this.asyncExecutor.getMaxPoolSize());
    log.trace("CorePoolSize = " + this.asyncExecutor.getCorePoolSize());
    log.trace("QueueCapacity = " + this.asyncExecutor.getThreadPoolExecutor().getQueue().size());
    log.trace("KeepAlive = " + this.asyncExecutor.getKeepAliveSeconds());
    log.trace("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
  }

  @Override
  @Async
  public Future<Void> deploy(
      NetworkServiceDescriptor networkServiceDescriptor,
      NetworkServiceRecord networkServiceRecord,
      DeployNSRBody body)
      throws NotFoundException {

    try {

      log.debug("Ordered: " + ordered);
      if (ordered) {
        vnfrNames.put(networkServiceRecord.getId(), new HashMap<String, Integer>());
        log.debug("here");
        Map<String, Integer> vnfrNamesWeighted = vnfrNames.get(networkServiceRecord.getId());
        fillVnfrNames(networkServiceDescriptor, vnfrNamesWeighted);
        vnfrNames.put(networkServiceRecord.getId(), sortByValue(vnfrNamesWeighted));

        log.debug("VNFRs ordered by dependencies: " + vnfrNames.get(networkServiceRecord.getId()));
      }

      for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
        log.debug("Processing VNFD: " + vnfd.getName());

        Map<String, Collection<VimInstance>> vimInstances = new HashMap<>();

        for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
          vimInstances.put(vdu.getId(), new ArrayList<VimInstance>());
          Collection<String> instanceNames;
          if (body == null
              || body.getVduVimInstances() == null
              || body.getVduVimInstances().get(vdu.getName()) == null) {
            instanceNames = vdu.getVimInstanceName();
          } else {
            instanceNames = body.getVduVimInstances().get(vdu.getName());
          }
          for (String vimInstanceName : instanceNames) {
            log.debug("Looking for " + vimInstanceName);
            VimInstance vimInstance = null;

            for (VimInstance vi : vimInstanceRepository.findByProjectId(vdu.getProjectId())) {
              if (vimInstanceName.equals(vi.getName())) {
                vimInstance = vi;
              }
            }

            vimInstances.get(vdu.getId()).add(vimInstance);
          }
        }
        for (Entry<String, Collection<VimInstance>> vimInstance : vimInstances.entrySet()) {

          if (vimInstance.getValue().isEmpty()) {
            for (VimInstance vimInstance1 :
                vimInstanceRepository.findByProjectId(networkServiceDescriptor.getProjectId())) {
              vimInstance.getValue().add(vimInstance1);
            }
          }
          for (VimInstance vi : vimInstance.getValue()) {
            log.debug("\t" + vi.getName());
          }
          log.debug("~~~~~~");
        }

        //Creating the extension
        Map<String, String> extension = getExtension();

        extension.put("nsr-id", networkServiceRecord.getId());

        NFVMessage message;
        HashSet<Key> keys;
        if (body.getKeys() != null) {
          keys = new HashSet<>(body.getKeys());
        } else {
          keys = new HashSet<>();
        }
        if (vnfd.getVnfPackageLocation() != null) {
          VNFPackage vnfPackage = vnfPackageRepository.findFirstById(vnfd.getVnfPackageLocation());
          message =
              new OrVnfmInstantiateMessage(
                  vnfd,
                  getDeploymentFlavour(vnfd),
                  vnfd.getName(),
                  networkServiceRecord.getVlr(),
                  extension,
                  vimInstances,
                  keys,
                  vnfPackage);
        } else {
          message =
              new OrVnfmInstantiateMessage(
                  vnfd,
                  getDeploymentFlavour(vnfd),
                  vnfd.getName(),
                  networkServiceRecord.getVlr(),
                  extension,
                  vimInstances,
                  keys,
                  null);
        }
        VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(vnfd.getEndpoint());
        if (endpoint == null) {
          throw new NotFoundException(
              "VnfManager of type "
                  + vnfd.getType()
                  + " (endpoint = "
                  + vnfd.getEndpoint()
                  + ") is not registered");
        }

        VnfmSender vnfmSender;
        try {
          vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
        } catch (BeansException e) {
          throw new NotFoundException(e);
        }
        vnfmSender.sendCommand(message, endpoint);
        log.info("Sent " + message.getAction() + " to VNF: " + vnfd.getName());
      }
      return new AsyncResult<>(null);
    } catch (Exception e) {
      log.error("", e);
      throw e;
    }
  }

  private Map<String, String> getExtension() {
    Map<String, String> extension = new HashMap<>();
    extension.put("brokerIp", brokerIp.trim());
    extension.put("brokerPort", brokerPort.trim());
    extension.put("monitoringIp", monitoringIp.trim());
    extension.put("timezone", timezone);
    extension.put("emsVersion", emsVersion);
    extension.put("username", username);
    extension.put("password", password);
    extension.put("exchangeName", "openbaton-exchange");
    extension.put("emsHeartbeat", emsHeartbeat);
    extension.put("emsAutodelete", emsAutodelete);
    return extension;
  }

  private void fillVnfrNames(
      NetworkServiceDescriptor networkServiceDescriptor, Map<String, Integer> vnfrNamesWeighted) {

    log.trace("Checking NSD \n\n\n" + networkServiceDescriptor);

    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {
      String virtualNetworkFunctionDescriptorName = virtualNetworkFunctionDescriptor.getName();
      int weightForVNFR =
          getWeightForVNFR(virtualNetworkFunctionDescriptor, networkServiceDescriptor);
      vnfrNamesWeighted.put(virtualNetworkFunctionDescriptorName, weightForVNFR);
      log.debug("Set weight for " + virtualNetworkFunctionDescriptorName + " to " + weightForVNFR);
    }
  }

  private int getWeightForVNFR(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      NetworkServiceDescriptor networkServiceDescriptor) {
    int result = 0;
    for (VNFDependency dependency : networkServiceDescriptor.getVnf_dependency()) {
      if (dependency.getTarget().getName().equals(virtualNetworkFunctionDescriptor.getName())) {
        result++;
        result += getWeightForVNFR(dependency.getSource(), networkServiceDescriptor);
      }
    }

    return result;
  }

  //As a default operation of the NFVO, it get always the first DeploymentFlavour!
  private VNFDeploymentFlavour getDeploymentFlavour(VirtualNetworkFunctionDescriptor vnfd)
      throws NotFoundException {
    if (!vnfd.getDeployment_flavour().iterator().hasNext()) {
      throw new NotFoundException("There are no DeploymentFlavour in vnfd: " + vnfd.getName());
    }
    return vnfd.getDeployment_flavour().iterator().next();
  }

  @Override
  public VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException {
    String senderName = endpointType.toString().toLowerCase() + "VnfmSender";
    return (VnfmSender) this.context.getBean(senderName);
  }

  @Override
  public String executeAction(NFVMessage nfvMessage)
      throws ExecutionException, InterruptedException {

    String actionName = nfvMessage.getAction().toString().replace("_", "").toLowerCase();
    String beanName = actionName + "Task";
    log.debug("Looking for bean called: " + beanName);
    AbstractTask task = (AbstractTask) context.getBean(beanName);
    log.trace("message: " + nfvMessage);
    task.setAction(nfvMessage.getAction());

    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
    if (nfvMessage.getAction().ordinal() == Action.ERROR.ordinal()) {
      VnfmOrErrorMessage vnfmOrErrorMessage = (VnfmOrErrorMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrErrorMessage.getVirtualNetworkFunctionRecord();
      Exception e = vnfmOrErrorMessage.getException();
      ((ErrorTask) task).setException(e);
      ((ErrorTask) task).setNsrId(vnfmOrErrorMessage.getNsrId());
    } else if (nfvMessage.getAction().ordinal() == Action.INSTANTIATE.ordinal()) {
      VnfmOrInstantiateMessage vnfmOrInstantiate = (VnfmOrInstantiateMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrInstantiate.getVirtualNetworkFunctionRecord();
    } else if (nfvMessage.getAction().ordinal() == Action.SCALED.ordinal()) {
      VnfmOrScaledMessage vnfmOrScaled = (VnfmOrScaledMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrScaled.getVirtualNetworkFunctionRecord();
      ((ScaledTask) task).setVnfcInstance(vnfmOrScaled.getVnfcInstance());
    } else if (nfvMessage.getAction().ordinal() == Action.HEAL.ordinal()) {
      VnfmOrHealedMessage vnfmOrHealedMessage = (VnfmOrHealedMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrHealedMessage.getVirtualNetworkFunctionRecord();
      ((HealTask) task).setVnfcInstance(vnfmOrHealedMessage.getVnfcInstance());
      ((HealTask) task).setCause(vnfmOrHealedMessage.getCause());
    } else if (nfvMessage.getAction().ordinal() == Action.SCALING.ordinal()) {
      VnfmOrScalingMessage vnfmOrScalingMessage = (VnfmOrScalingMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrScalingMessage.getVirtualNetworkFunctionRecord();
      ((ScalingTask) task).setUserdata(vnfmOrScalingMessage.getUserData());
    } else if (nfvMessage.getAction().ordinal() == Action.ALLOCATE_RESOURCES.ordinal()) {
      VnfmOrAllocateResourcesMessage vnfmOrAllocateResourcesMessage =
          (VnfmOrAllocateResourcesMessage) nfvMessage;
      virtualNetworkFunctionRecord =
          vnfmOrAllocateResourcesMessage.getVirtualNetworkFunctionRecord();
      Map<String, VimInstance> vimChosen = vnfmOrAllocateResourcesMessage.getVimInstances();
      ((AllocateresourcesTask) task).setVims(vimChosen);
      ((AllocateresourcesTask) task)
          .setKeys(new HashSet<>(vnfmOrAllocateResourcesMessage.getKeyPairs()));
      ((AllocateresourcesTask) task).setUserData(vnfmOrAllocateResourcesMessage.getUserdata());
    } else if (nfvMessage.getAction().ordinal() == Action.START.ordinal()) {
      VnfmOrStartStopMessage vnfmOrStartStopMessage = (VnfmOrStartStopMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrStartStopMessage.getVirtualNetworkFunctionRecord();
      VNFCInstance vnfcInstance = vnfmOrStartStopMessage.getVnfcInstance();
      if (vnfcInstance != null) {
        ((StartTask) task).setVnfcInstance(vnfcInstance);
      }
    } else if (nfvMessage.getAction().ordinal() == Action.STOP.ordinal()) {
      VnfmOrStartStopMessage vnfmOrStartStopMessage = (VnfmOrStartStopMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrStartStopMessage.getVirtualNetworkFunctionRecord();
      VNFCInstance vnfcInstance = vnfmOrStartStopMessage.getVnfcInstance();
      if (vnfcInstance != null) {
        ((StopTask) task).setVnfcInstance(vnfcInstance);
      }
    } else {
      VnfmOrGenericMessage vnfmOrGeneric = (VnfmOrGenericMessage) nfvMessage;
      virtualNetworkFunctionRecord = vnfmOrGeneric.getVirtualNetworkFunctionRecord();
    }

    if (virtualNetworkFunctionRecord != null) {
      if (virtualNetworkFunctionRecord.getParent_ns_id() != null) {
        if (!nsrRepository.exists(virtualNetworkFunctionRecord.getParent_ns_id())) {
          return null;
        } else {
          virtualNetworkFunctionRecord.setProjectId(
              nsrRepository
                  .findFirstById(virtualNetworkFunctionRecord.getParent_ns_id())
                  .getProjectId());
          for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            vdu.setProjectId(
                nsrRepository
                    .findFirstById(virtualNetworkFunctionRecord.getParent_ns_id())
                    .getProjectId());
          }
        }
      }

      virtualNetworkFunctionRecord.setTask(actionName);
      task.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);

      log.info(
          "Executing Task "
              + beanName
              + " for vnfr "
              + virtualNetworkFunctionRecord.getName()
              + ". Cyclic="
              + virtualNetworkFunctionRecord.hasCyclicDependency());
    }
    log.trace("AsyncExecutor is: " + asyncExecutor);
    if (isaReturningTask(nfvMessage.getAction())) {
      return gson.toJson(asyncExecutor.submit(task).get());
    } else {
      asyncExecutor.submit(task);
      return null;
    }
  }

  private boolean isaReturningTask(Action action) {
    switch (action) {
      case ALLOCATE_RESOURCES:
      case GRANT_OPERATION:
      case SCALING:
      case UPDATEVNFR:
        return true;
      default:
        return false;
    }
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
          if (nsdRepository
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
            if (networkServiceRecord.getTask().contains("Scaling in")) {
              networkServiceRecord.setTask("Scaled in");
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(Action.SCALE_IN, networkServiceRecord);
            } else if (networkServiceRecord.getTask().contains("Scaling out")) {
              networkServiceRecord.setTask("Scaled out");
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(Action.SCALE_OUT, networkServiceRecord);
            } else if (networkServiceRecord.getTask().contains("Healing")) {
              networkServiceRecord.setTask("Healed");
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(Action.HEAL, networkServiceRecord);
            } else {
              networkServiceRecord.setTask("Onboarded");
              networkServiceRecord = nsrRepository.save(networkServiceRecord);
              publishEvent(Action.INSTANTIATE_FINISH, networkServiceRecord);
            }
          } catch (OptimisticLockingFailureException e) {
            log.error(
                "FFFF OptimisticLockingFailureException while setting the task of the NSR. Don't worry we will try it again.");
            savedNsr = false;
          }
        } else {
          log.debug("Nsr is ACTIVE but not all vnfr have been received");
        }
      } while (!savedNsr);

    } else if (status.ordinal() == Status.TERMINATED.ordinal()) {
      publishEvent(Action.RELEASE_RESOURCES_FINISH, networkServiceRecord);
      nsrRepository.delete(networkServiceRecord);
    }

    log.trace("Thread: " + Thread.currentThread().getId() + " finished findAndSet");
  }

  private NetworkServiceRecord safeSaveNetworkServiceRecord(
      NetworkServiceRecord networkServiceRecord) {
    boolean foundAndSet = false;

    while (!foundAndSet) {
      try {
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        foundAndSet = true;
      } catch (OptimisticLockingFailureException ignored) {
        log.debug(
            "OptimisticLockingFailureException during findAndSet. Don't worry we will try it again.");
      }
    }
    return networkServiceRecord;
  }

  private void publishEvent(Action action, Serializable payload) {
    ApplicationEventNFVO event = new ApplicationEventNFVO(action, payload);
    EventNFVO eventNFVO = new EventNFVO(this);
    eventNFVO.setEventNFVO(event);
    log.debug("Publishing event: " + event);
    publisher.publishEvent(eventNFVO);
  }

  @Override
  @Async
  public Future<Void> sendMessageToVNFR(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, NFVMessage nfvMessage)
      throws NotFoundException {
    VnfmManagerEndpoint endpoint =
        vnfmRegister.getVnfm(virtualNetworkFunctionRecordDest.getEndpoint());
    if (endpoint == null) {
      throw new NotFoundException(
          "VnfManager of type "
              + virtualNetworkFunctionRecordDest.getType()
              + " (endpoint = "
              + virtualNetworkFunctionRecordDest.getEndpoint()
              + ") is not registered");
    }
    VnfmSender vnfmSender;
    try {
      vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    log.debug(
        "Sending message "
            + nfvMessage.getAction()
            + " to "
            + virtualNetworkFunctionRecordDest.getName());
    vnfmSender.sendCommand(nfvMessage, endpoint);
    return new AsyncResult<>(null);
  }

  @Override
  @Async
  public Future<Void> release(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws NotFoundException {
    VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint());
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

      vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    vnfmSender.sendCommand(orVnfmGenericMessage, endpoint);
    return new AsyncResult<>(null);
  }

  @Override
  @Async
  public Future<Void> addVnfc(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent component,
      VNFRecordDependency dependency,
      String mode)
      throws NotFoundException {
    VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint());

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
    message.setExtension(getExtension());
    message.setDependency(dependency);
    message.setMode(mode);

    log.debug("SCALE_OUT MESSAGE IS: \n" + message);

    VnfmSender vnfmSender;
    try {

      vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    vnfmSender.sendCommand(message, endpoint);
    return new AsyncResult<>(null);
  }

  @Override
  @Async
  public Future<Void> removeVnfcDependency(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance)
      throws NotFoundException {
    VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint());
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

      vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
    } catch (BeansException e) {
      throw new NotFoundException(e);
    }

    vnfmSender.sendCommand(message, endpoint);
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
    publishEvent(event.getEventNFVO().getAction(), virtualNetworkFunctionRecord);
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
  public void terminate(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    ReleaseresourcesTask task = (ReleaseresourcesTask) context.getBean("releaseresourcesTask");
    task.setAction(Action.RELEASE_RESOURCES);
    task.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);

    this.asyncExecutor.submit(task);
  }

  @Override
  public void updateScript(Script script, String vnfPackageId) throws NotFoundException {

    for (VirtualNetworkFunctionDescriptor vnfd : vnfdRepository.findAll()) {
      if (vnfd.getVnfPackageLocation().equals(vnfPackageId)) {
        for (VirtualNetworkFunctionRecord vnfr : vnfrRepository.findAll()) {
          OrVnfmUpdateMessage orVnfmUpdateMessage = new OrVnfmUpdateMessage();
          orVnfmUpdateMessage.setScript(script);
          orVnfmUpdateMessage.setVnfr(vnfr);
          if (vnfr.getPackageId().equals(vnfPackageId)) {
            sendMessageToVNFR(vnfr, orVnfmUpdateMessage);
          }
        }
      }
    }
  }
}
