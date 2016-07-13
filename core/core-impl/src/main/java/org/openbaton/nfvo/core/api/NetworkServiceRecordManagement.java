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

package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.LinkStatus;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.Network;
import org.openbaton.catalogue.nfvo.Subnet;
import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmHealVNFRequestMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.QuotaExceededException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongStatusException;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.openbaton.nfvo.core.interfaces.NetworkManagement;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.core.utils.NSRUtils;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFCRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.nfvo.repositories.VNFRecordDependencyRepository;
import org.openbaton.nfvo.repositories.VduRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties
public class NetworkServiceRecordManagement
    implements org.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private EventDispatcher publisher;

  @Autowired private NetworkServiceRecordRepository nsrRepository;

  @Autowired private NetworkServiceDescriptorRepository nsdRepository;

  @Autowired private VNFRRepository vnfrRepository;

  @Autowired private VNFRecordDependencyRepository vnfRecordDependencyRepository;

  @Autowired private ConfigurationManagement configurationManagement;

  @Autowired private NSDUtils nsdUtils;

  @Autowired private VnfmManager vnfmManager;

  @Autowired private ResourceManagement resourceManagement;

  @Autowired private NetworkManagement networkManagement;

  @Autowired private DependencyManagement dependencyManagement;

  @Autowired private VNFCRepository vnfcRepository;

  @Autowired private VduRepository vduRepository;

  @Autowired private VimRepository vimInstanceRepository;

  @Autowired private VnfmEndpointRepository vnfmManagerEndpointRepository;

  private ThreadPoolTaskExecutor asyncExecutor;

  @Value("${nfvo.delete.vnfr.wait.timeout:60}")
  private int timeout;

  @Value("${nfvo.delete.vnfr.wait:false}")
  private boolean removeAfterTimeout;

  @Value("${nfvo.delete.all-status:true}")
  private boolean deleteInAllStatus;

  @PostConstruct
  private void init() {
    if (removeAfterTimeout) {
      asyncExecutor = new ThreadPoolTaskExecutor();
      asyncExecutor.setThreadNamePrefix("OpenbatonTask-");
      asyncExecutor.setMaxPoolSize(30);
      asyncExecutor.setCorePoolSize(5);
      asyncExecutor.setQueueCapacity(0);
      asyncExecutor.setKeepAliveSeconds(20);
      asyncExecutor.initialize();
    }
  }

  @Override
  public NetworkServiceRecord onboard(String idNsd, String projectID)
      throws InterruptedException, ExecutionException, VimException, NotFoundException,
          BadFormatException, VimDriverException, QuotaExceededException, PluginException {
    log.info("Looking for NetworkServiceDescriptor with id: " + idNsd);
    NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(idNsd);
    if (!networkServiceDescriptor.getProjectId().equals(projectID))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    if (networkServiceDescriptor == null) {
      throw new NotFoundException("NSD with id " + idNsd + " was not found");
    }
    return deployNSR(networkServiceDescriptor, projectID);
  }

  @Override
  public NetworkServiceRecord onboard(
      NetworkServiceDescriptor networkServiceDescriptor, String projectId)
      throws ExecutionException, InterruptedException, VimException, NotFoundException,
          BadFormatException, VimDriverException, QuotaExceededException, PluginException {
    networkServiceDescriptor.setProjectId(projectId);
    nsdUtils.fetchVimInstances(networkServiceDescriptor, projectId);
    return deployNSR(networkServiceDescriptor, projectId);
  }

  public void deleteVNFRecord(String idNsr, String idVnf, String projectId) {
    //TODO the logic of this request for the moment deletes only the VNFR from the DB, need to be removed from the running NetworkServiceRecord
    if (!nsrRepository.findFirstById(idNsr).getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    nsrRepository.deleteVNFRecord(idNsr, idVnf);
  }

  /**
   * Returns the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
   *
   * @param idNsr of Nsr
   * @param idVnf of VirtualNetworkFunctionRecord
   * @param projectId
   * @return VirtualNetworkFunctionRecord selected
   */
  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
      String idNsr, String idVnf, String projectId) throws NotFoundException {
    NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(idNsr);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    if (networkServiceRecord == null) {
      throw new NotFoundException("NSR with id " + idNsr + " was not found");
    }
    for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord :
        networkServiceRecord.getVnfr()) {
      if (virtualNetworkFunctionRecord.getId().equals(idVnf)) return virtualNetworkFunctionRecord;
    }
    throw new NotFoundException("VNFR with id " + idVnf + " was not found");
  }

  /**
   * Deletes the VNFDependency with idVnfr into NSR with idNsr
   *
   * @param idNsr of NSR
   * @param idVnfd of VNFDependency
   * @param projectId
   */
  @Override
  public void deleteVNFDependency(String idNsr, String idVnfd, String projectId) {
    //TODO the logic of this request for the moment deletes only the VNFR from the DB, need to be removed from the running NetworkServiceRecord
    if (!nsrRepository.findFirstById(idNsr).getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    nsrRepository.deleteVNFDependency(idNsr, idVnfd);
  }

  @Override
  public void addVNFCInstance(String id, String idVnf, VNFComponent component, String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.info("Adding new VNFCInstance to VNFR with id: " + idVnf);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualDeploymentUnit virtualDeploymentUnit =
        virtualNetworkFunctionRecord.getVdu().iterator().next();
    if (virtualDeploymentUnit == null) {
      throw new NotFoundException("No VirtualDeploymentUnit found");
    }

    if (virtualDeploymentUnit.getScale_in_out()
        == virtualDeploymentUnit.getVnfc_instance().size()) {
      throw new WrongStatusException(
          "The VirtualDeploymentUnit chosen has reached the maximum number of VNFCInstance");
    }
    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.save(networkServiceRecord);
    scaleOUT(
        networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, component, "");
  }

  @Override
  public void addVNFCInstance(
      String id, String idVnf, String idVdu, VNFComponent component, String mode, String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.info("Adding new VNFCInstance to VNFR with id: " + idVnf);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    if (virtualDeploymentUnit.getScale_in_out()
        == virtualDeploymentUnit.getVnfc_instance().size()) {
      throw new WrongStatusException(
          "The VirtualDeploymentUnit chosen has reached the maximum number of VNFCInstance");
    }

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.save(networkServiceRecord);
    scaleOUT(
        networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, component, mode);
  }

  private void scaleOUT(
      NetworkServiceRecord networkServiceRecord,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VirtualDeploymentUnit virtualDeploymentUnit,
      VNFComponent component,
      String mode)
      throws BadFormatException, NotFoundException {
    List<String> componentNetworks = new ArrayList<>();

    for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
      componentNetworks.add(connectionPoint.getVirtual_link_reference());
    }

    List<String> vnfrNetworks = new ArrayList<>();

    for (InternalVirtualLink virtualLink : virtualNetworkFunctionRecord.getVirtual_link()) {
      vnfrNetworks.add(virtualLink.getName());
    }

    if (!vnfrNetworks.containsAll(componentNetworks)) {
      throw new BadFormatException(
          "Not all the network exist in the InternalVirtualLinks. They need to be included in these names: "
              + vnfrNetworks);
    }

    log.info(
        "Adding VNFComponent to VirtualNetworkFunctionRecord "
            + virtualNetworkFunctionRecord.getName());

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      if (vdu.getId().equals(virtualDeploymentUnit.getId())) {
        vdu.getVnfc().add(component);
      }
    }

    //        virtualDeploymentUnit.getVnfc().add(component);
    vnfcRepository.save(component);
    nsrRepository.save(networkServiceRecord);
    log.debug("new VNFComponent is " + component);

    VNFRecordDependency dependencyTarget =
        dependencyManagement.getDependencyForAVNFRecordTarget(virtualNetworkFunctionRecord);

    log.debug("Found Dependency: " + dependencyTarget);

    vnfmManager.addVnfc(virtualNetworkFunctionRecord, component, dependencyTarget, mode);
  }

  @Override
  public void deleteVNFCInstance(String id, String idVnf, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException {
    log.info("Removing VNFCInstance from VNFR with id: " + idVnf);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");

    VirtualDeploymentUnit virtualDeploymentUnit = null;

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      if (vdu.getProjectId() != null && vdu.getProjectId().equals(projectId)) {
        virtualDeploymentUnit = vdu;
        break;
      }
    }

    if (virtualDeploymentUnit == null) {
      throw new NotFoundException("No VirtualDeploymentUnit found");
    }

    if (virtualDeploymentUnit.getVnfc_instance().size() == 1) {
      throw new WrongStatusException(
          "The VirtualDeploymentUnit chosen has reached the minimum number of VNFCInstance");
    }

    VNFCInstance vnfcInstance = virtualDeploymentUnit.getVnfc_instance().iterator().next();
    if (vnfcInstance == null) throw new NotFoundException("No VNFCInstance was found");

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.save(networkServiceRecord);
    scaleIn(
        networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, vnfcInstance);
  }

  @Override
  public void deleteVNFCInstance(String id, String idVnf, String idVdu, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException {
    log.info("Removing VNFCInstance from VNFR with id: " + idVnf + " in vdu: " + idVdu);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");

    VirtualDeploymentUnit virtualDeploymentUnit = null;

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      if (vdu.getId().equals(idVdu)
          && vdu.getProjectId() != null
          && vdu.getProjectId().equals(projectId)) {
        virtualDeploymentUnit = vdu;
      }
    }

    if (virtualDeploymentUnit == null) {
      throw new NotFoundException("No VirtualDeploymentUnit found");
    }

    if (virtualDeploymentUnit.getVnfc_instance().size() == 1) {
      throw new WrongStatusException(
          "The VirtualDeploymentUnit chosen has reached the minimum number of VNFCInstance");
    }

    VNFCInstance vnfcInstance = virtualDeploymentUnit.getVnfc_instance().iterator().next();
    if (vnfcInstance == null) throw new NotFoundException("No VNFCInstance was not found");

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.save(networkServiceRecord);
    scaleIn(
        networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, vnfcInstance);
  }

  @Override
  public List<NetworkServiceRecord> queryByProjectId(String projectId) {
    return nsrRepository.findByProjectId(projectId);
  }

  @Override
  public void deleteVNFCInstance(
      String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException {
    log.info(
        "Removing VNFCInstance with id: "
            + idVNFCI
            + " from VNFR with id: "
            + idVnf
            + " in vdu: "
            + idVdu);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VDU not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    if (virtualDeploymentUnit.getVnfc_instance().size() == 1) {

      throw new WrongStatusException(
          "The VirtualDeploymentUnit chosen has reached the minimum number of VNFCInstance");
    }

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.save(networkServiceRecord);
    scaleIn(
        networkServiceRecord,
        virtualNetworkFunctionRecord,
        virtualDeploymentUnit,
        getVNFCI(virtualDeploymentUnit, idVNFCI));
  }

  @Override
  public void switchToRedundantVNFCInstance(
      String id,
      String idVnf,
      String idVdu,
      String idVNFC,
      String mode,
      VNFCInstance failedVnfcInstance,
      String projectId)
      throws NotFoundException, WrongStatusException {
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VDU not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VNFCInstance standByVNFCInstance = null;
    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
      log.debug("current vnfcinstance " + vnfcInstance + " in state" + vnfcInstance.getState());
      if (vnfcInstance.getState() != null && vnfcInstance.getState().equals(mode)) {
        standByVNFCInstance = vnfcInstance;
        log.debug("VNFComponentInstance in " + mode + " mode FOUND :" + standByVNFCInstance);
      }
      if (vnfcInstance.getId().equals(failedVnfcInstance.getId())) {
        vnfcInstance.setState("failed");
        log.debug(
            "The vnfcInstance: "
                + vnfcInstance.getHostname()
                + " is set to '"
                + vnfcInstance.getState()
                + "' state");
      }
    }
    if (standByVNFCInstance == null)
      throw new NotFoundException(
          "No VNFCInstance in "
              + mode
              + " mode found, so switch to redundant VNFC is not possibile");

    //save the new state of the failedVnfcInstance
    nsrRepository.save(networkServiceRecord);

    OrVnfmHealVNFRequestMessage healMessage = new OrVnfmHealVNFRequestMessage();
    healMessage.setAction(Action.HEAL);
    healMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
    healMessage.setVnfcInstance(standByVNFCInstance);
    healMessage.setCause("switchToStandby");

    vnfmManager.sendMessageToVNFR(virtualNetworkFunctionRecord, healMessage);
  }

  private VNFCInstance getVNFCI(VirtualDeploymentUnit virtualDeploymentUnit, String idVNFCI)
      throws NotFoundException {

    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance())
      if (idVNFCI == null || idVNFCI.equals(vnfcInstance.getId())) return vnfcInstance;

    throw new NotFoundException("VNFCInstance with id " + idVNFCI + " was not found");
  }

  private void scaleIn(
      NetworkServiceRecord networkServiceRecord,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VirtualDeploymentUnit virtualDeploymentUnit,
      VNFCInstance vnfcInstance)
      throws NotFoundException, InterruptedException, ExecutionException, VimException,
          PluginException {
    List<VNFRecordDependency> dependencySource =
        dependencyManagement.getDependencyForAVNFRecordSource(virtualNetworkFunctionRecord);

    if (!dependencySource.isEmpty()) {
      for (VNFRecordDependency dependency : dependencySource) {
        List<String> paramsToRemove = new ArrayList<>();
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord1 :
            networkServiceRecord.getVnfr())
          if (virtualNetworkFunctionRecord1.getName().equals(dependency.getTarget())) {
            vnfmManager.removeVnfcDependency(virtualNetworkFunctionRecord1, vnfcInstance);
            for (Entry<String, VNFCDependencyParameters> parametersEntry :
                dependency.getVnfcParameters().entrySet()) {
              log.debug("Parameter: " + parametersEntry);
              if (parametersEntry.getValue() != null)
                parametersEntry.getValue().getParameters().remove(vnfcInstance.getId());
            }
          }
        for (String paramToRemove : paramsToRemove)
          dependency.getVnfcParameters().remove(paramToRemove);

        vnfRecordDependencyRepository.save(dependency);
      }
    }

    resourceManagement.release(virtualDeploymentUnit, vnfcInstance);
    for (Ip ip : vnfcInstance.getIps()) {
      virtualNetworkFunctionRecord.getVnf_address().remove(ip.getIp());
    }
    virtualDeploymentUnit.getVnfc().remove(vnfcInstance.getVnfComponent());
    virtualDeploymentUnit.getVnfc_instance().remove(vnfcInstance);

    vduRepository.save(virtualDeploymentUnit);

    ApplicationEventNFVO event =
        new ApplicationEventNFVO(Action.SCALE_IN, virtualNetworkFunctionRecord);
    EventNFVO eventNFVO = new EventNFVO(this);
    eventNFVO.setEventNFVO(event);
    log.debug("Publishing event: " + event);
    publisher.dispatchEvent(eventNFVO);

    networkServiceRecord.setStatus(Status.ACTIVE);
    nsrRepository.save(networkServiceRecord);
  }

  private VirtualDeploymentUnit getVirtualDeploymentUnit(
      String idVdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord)
      throws NotFoundException {
    VirtualDeploymentUnit virtualDeploymentUnit = null;
    for (VirtualDeploymentUnit virtualDeploymentUnit1 : virtualNetworkFunctionRecord.getVdu()) {
      if (virtualDeploymentUnit1.getId().equals(idVdu)) {
        virtualDeploymentUnit = virtualDeploymentUnit1;
      }
    }
    if (virtualDeploymentUnit == null)
      throw new NotFoundException("No VirtualDeploymentUnit found with id " + idVdu);
    return virtualDeploymentUnit;
  }

  private VNFCInstance getVNFCInstance(String idVNFCInstance, VirtualDeploymentUnit vdu)
      throws NotFoundException {
    VNFCInstance vnfcInstance = null;
    for (VNFCInstance currentVnfcInstance : vdu.getVnfc_instance()) {
      if (currentVnfcInstance.getId().equals(idVNFCInstance)) {
        vnfcInstance = currentVnfcInstance;
        break;
      }
    }
    if (vnfcInstance == null)
      throw new NotFoundException("No VnfcInstance found with id " + idVNFCInstance);
    return vnfcInstance;
  }

  private VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
      String idVnf, NetworkServiceRecord networkServiceRecord) throws NotFoundException {
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = null;
    for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord1 :
        networkServiceRecord.getVnfr()) {
      if (virtualNetworkFunctionRecord1.getId().equals(idVnf)) {
        virtualNetworkFunctionRecord = virtualNetworkFunctionRecord1;
        break;
      }
    }
    if (virtualNetworkFunctionRecord == null)
      throw new NotFoundException("No VirtualNetworkFunctionRecord found with id " + idVnf);
    return virtualNetworkFunctionRecord;
  }

  private synchronized NetworkServiceRecord getNetworkServiceRecordInActiveState(String id)
      throws NotFoundException, WrongStatusException {
    NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(id);
    if (networkServiceRecord == null)
      throw new NotFoundException("No NetworkServiceRecord found with id " + id);

    if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal()) {
      throw new WrongStatusException("NetworkServiceDescriptor must be in ACTIVE state");
    }

    return networkServiceRecord;
  }

  private NetworkServiceRecord deployNSR(
      NetworkServiceDescriptor networkServiceDescriptor, String projectID)
      throws NotFoundException, BadFormatException, VimException, PluginException {
    log.info("Fetched NetworkServiceDescriptor: " + networkServiceDescriptor.getName());
    log.info("VNFD are: ");
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd())
      log.debug("\t" + virtualNetworkFunctionDescriptor.getName());

    log.info("Checking if all vnfm are registered and active");
    Iterable<VnfmManagerEndpoint> endpoints = vnfmManagerEndpointRepository.findAll();

    nsdUtils.checkEndpoint(networkServiceDescriptor, endpoints);

    log.trace("Fetched NetworkServiceDescriptor: " + networkServiceDescriptor);
    NetworkServiceRecord networkServiceRecord;
    networkServiceRecord = NSRUtils.createNetworkServiceRecord(networkServiceDescriptor);
    log.trace("Creating " + networkServiceRecord);

    for (VirtualLinkRecord vlr : networkServiceRecord.getVlr()) {
      for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
        for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
          for (String vimInstanceName : vdu.getVimInstanceName()) {

            VimInstance vimInstance = null;

            for (VimInstance vi : vimInstanceRepository.findByProjectId(vdu.getProjectId())) {
              if (vimInstanceName.equals(vi.getName())) vimInstance = vi;
            }

            for (VNFComponent vnfc : vdu.getVnfc()) {
              for (VNFDConnectionPoint vnfdConnectionPoint : vnfc.getConnection_point()) {
                if (vnfdConnectionPoint.getVirtual_link_reference().equals(vlr.getName())) {
                  boolean networkExists = false;
                  for (Network network : vimInstance.getNetworks()) {
                    if (network.getName().equals(vlr.getName())
                        || network.getExtId().equals(vlr.getName())) {
                      networkExists = true;
                      vlr.setStatus(LinkStatus.NORMALOPERATION);
                      vlr.setVim_id(vdu.getId());
                      vlr.setExtId(network.getExtId());
                      vlr.getConnection().add(vnfdConnectionPoint.getId());
                      break;
                    }
                  }
                  if (!networkExists) {
                    Network network = new Network();
                    network.setName(vlr.getName());
                    network.setSubnets(new HashSet<Subnet>());
                    network = networkManagement.add(vimInstance, network);
                    vlr.setStatus(LinkStatus.NORMALOPERATION);
                    vlr.setVim_id(vdu.getId());
                    vlr.setExtId(network.getExtId());
                    vlr.getConnection().add(vnfdConnectionPoint.getId());
                  }
                }
              }
            }
          }
        }
      }
    }

    NSRUtils.setDependencies(networkServiceDescriptor, networkServiceRecord);

    networkServiceRecord.setProjectId(projectID);
    networkServiceRecord = nsrRepository.save(networkServiceRecord);
    log.debug(
        "Persited NSR "
            + networkServiceRecord.getName()
            + ". Got id: "
            + networkServiceRecord.getId());

    /**
     * now check for the requires pointing to the nfvo
     */
    //TODO check where to put this
    if (networkServiceRecord.getVnfr() != null)
      for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord :
          networkServiceRecord.getVnfr()) {
        for (ConfigurationParameter configurationParameter :
            virtualNetworkFunctionRecord.getRequires().getConfigurationParameters()) {
          log.debug("Checking parameter: " + configurationParameter.getConfKey());
          if (configurationParameter
              .getConfKey()
              .startsWith("nfvo:")) { //the parameters known from the nfvo
            String[] params = configurationParameter.getConfKey().split(":");
            for (ConfigurationParameter configurationParameterSystem :
                configurationManagement.queryByName("system").getConfigurationParameters()) {
              if (configurationParameterSystem.getConfKey().equals(params[1])) {
                log.debug("Found parameter: " + configurationParameterSystem);
                configurationParameter.setValue(configurationParameterSystem.getValue());
              }
            }
          }
        }
      }

    vnfmManager.deploy(networkServiceDescriptor, networkServiceRecord);
    log.debug("Returning NSR " + networkServiceRecord.getName());
    return networkServiceRecord;
  }

  @Override
  public NetworkServiceRecord update(NetworkServiceRecord newRsr, String idNsr, String projectId)
      throws NotFoundException {
    NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(idNsr);
    if (networkServiceRecord != null) {
      if (!networkServiceRecord.getProjectId().equals(projectId))
        throw new UnauthorizedUserException(
            "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    } else throw new NotFoundException("NetworkServiceRecord with id not found");
    newRsr = nsrRepository.save(newRsr);
    return newRsr;
  }

  @Override
  public Iterable<NetworkServiceRecord> query() {
    return nsrRepository.findAll();
  }

  @Override
  public void executeAction(
      NFVMessage nfvMessage,
      String nsrId,
      String idVnf,
      String idVdu,
      String idVNFCI,
      String projectId)
      throws NotFoundException {

    log.info("Executing action: " + nfvMessage.getAction() + " on VNF with id: " + idVnf);

    NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(nsrId);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VDU not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    VNFCInstance vnfcInstance = getVNFCInstance(idVNFCI, virtualDeploymentUnit);
    switch (nfvMessage.getAction()) {
      case HEAL:
        // Note: when we get a HEAL message from the API, it contains only the cause (no vnfr or vnfcInstance).
        // Here the vnfr and the vnfcInstance are set into the message, since they are updated.
        VnfmOrHealedMessage VnfmOrHealVNFRequestMessage = (VnfmOrHealedMessage) nfvMessage;
        log.debug("Received Heal message: " + VnfmOrHealVNFRequestMessage);
        VnfmOrHealVNFRequestMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
        VnfmOrHealVNFRequestMessage.setVnfcInstance(vnfcInstance);
        vnfmManager.sendMessageToVNFR(virtualNetworkFunctionRecord, VnfmOrHealVNFRequestMessage);
        break;
    }
  }

  @Override
  public NetworkServiceRecord query(String id, String projectId) {
    log.debug("Id is: " + id);
    NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(id);
    log.trace("found nsr = " + networkServiceRecord);
    log.debug(" project id is: " + projectId);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    return networkServiceRecord;
  }

  @Override
  public void delete(String id, String projectId) throws NotFoundException, WrongStatusException {
    log.info("Removing NSR with id: " + id);
    NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(id);
    if (!networkServiceRecord.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    if (networkServiceRecord == null) {
      throw new NotFoundException("NetworkServiceRecord with id " + id + " was not found");
    }

    if (!deleteInAllStatus) {
      if (networkServiceRecord.getStatus().ordinal() == Status.NULL.ordinal())
        throw new WrongStatusException(
            "The NetworkService "
                + networkServiceRecord.getId()
                + " is in the wrong state. ( Status= "
                + networkServiceRecord.getStatus()
                + " )");
      if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal()
          && networkServiceRecord.getStatus().ordinal() != Status.ERROR.ordinal())
        throw new WrongStatusException(
            "The NetworkService "
                + networkServiceRecord.getId()
                + " is in the wrong state. ( Status= "
                + networkServiceRecord.getStatus()
                + " )");
    }

    if (!networkServiceRecord.getVnfr().isEmpty()) {
      networkServiceRecord.setStatus(Status.TERMINATED); // TODO maybe terminating?
      for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord :
          networkServiceRecord.getVnfr()) {
        if (removeAfterTimeout) {
          VNFRTerminator terminator = new VNFRTerminator();
          terminator.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
          this.asyncExecutor.submit(terminator);
        }
        vnfmManager.release(virtualNetworkFunctionRecord);
      }
    } else nsrRepository.delete(networkServiceRecord.getId());
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  @ConfigurationProperties
  class VNFRTerminator implements Runnable {

    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
      return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
      this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(timeout * 1000);
        if (vnfrRepository.exists(virtualNetworkFunctionRecord.getId())) {
          virtualNetworkFunctionRecord =
              vnfrRepository.findFirstById(virtualNetworkFunctionRecord.getId());
          log.debug(
              "Terminating the VNFR not yet removed: " + virtualNetworkFunctionRecord.getName());
          vnfmManager.terminate(virtualNetworkFunctionRecord);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
