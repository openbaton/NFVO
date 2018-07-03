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

package org.openbaton.nfvo.core.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import org.openbaton.catalogue.api.DeployNSRBody;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.*;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmHealVNFRequestMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmStartStopMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.common.utils.viminstance.VimInstanceUtils;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.core.utils.NSRUtils;
import org.openbaton.nfvo.repositories.*;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.openbaton.vnfm.interfaces.state.VnfStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
@ConfigurationProperties
@SuppressWarnings({"unsafe", "unchecked"})
public class NetworkServiceRecordManagement
    implements org.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private ThreadPoolTaskExecutor executor;
  @Autowired private EventDispatcher publisher;
  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VNFRRepository vnfrRepository;
  @Autowired private VNFRecordDependencyRepository vnfRecordDependencyRepository;
  @Autowired private NSDUtils nsdUtils;
  @Autowired private VnfmManager vnfmManager;
  @Autowired private VnfStateHandler vnfStateHandler;
  @Autowired private ResourceManagement resourceManagement;
  @Autowired private DependencyManagement dependencyManagement;
  @Autowired private VNFCRepository vnfcRepository;
  @Autowired private VduRepository vduRepository;
  @Autowired private VimRepository vimInstanceRepository;
  @Autowired private VnfmEndpointRepository vnfmManagerEndpointRepository;
  @Autowired private VimBroker vimBroker;
  @Autowired private EntityManager entityManager;
  @Autowired private KeyRepository keyRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VimManagement vimManagement;

  @Value("${nfvo.delete.vnfr.wait.timeout:60}")
  private int timeout;

  @Value("${nfvo.delete.vnfr.wait:true}")
  private boolean removeAfterTimeout;

  @Value("${nfvo.delete.all-status:true}")
  private boolean deleteInAllStatus;

  @Value("${nfvo.quota.check:true}")
  private boolean isQuotaCheckEnabled;

  @Value("${nfvo.quota.check.failOnException:true}")
  private boolean failingQuotaCheckOnException;

  @Override
  public NetworkServiceRecord onboard(
      String idNsd,
      String projectID,
      List keys,
      Map<String, Set<String>> vduVimInstances,
      Map<String, Configuration> configurations,
      String monitoringIp)
      throws VimException, NotFoundException, PluginException, BadRequestException, IOException,
          AlreadyExistingException, BadFormatException, ExecutionException, InterruptedException {
    log.info("Looking for NetworkServiceDescriptor with ID: " + idNsd);
    NetworkServiceDescriptor networkServiceDescriptor =
        nsdRepository.findFirstByIdAndProjectId(idNsd, projectID);
    if (networkServiceDescriptor == null) {
      throw new NotFoundException("NSD with ID " + idNsd + " was not found");
    }

    // Detach instance of NSD to stop persistence
    if (networkServiceDescriptor.getId() != null) {
      entityManager.detach(networkServiceDescriptor);
    }

    DeployNSRBody body = new DeployNSRBody();
    body.setVduVimInstances(vduVimInstances);
    if (configurations == null) {
      body.setConfigurations(new HashMap());
    } else {
      body.setConfigurations(configurations);
    }
    if (keys == null) {
      body.setKeys(null);
    } else {
      Set<Key> keys1 = new LinkedHashSet<>();
      for (Object k : keys) {
        log.debug("Looking for keyname: " + k);
        Key key = keyRepository.findKey(projectID, (String) k);
        if (key == null) {
          throw new NotFoundException("No key found with name: " + k);
        }
        keys1.add(key);
      }
      body.setKeys(keys1);
      log.debug("Found keys: " + body.getKeys());
    }
    return deployNSR(networkServiceDescriptor, projectID, body, monitoringIp);
  }

  @Override
  public NetworkServiceRecord scaleOutNsr(
      String nsrId,
      String vnfdId,
      String projectId,
      List keys,
      Map vduVimInstances,
      Map configurations,
      String monitoringIp)
      throws NotFoundException, BadRequestException, InterruptedException, BadFormatException,
          ExecutionException, CyclicDependenciesException, NetworkServiceIntegrityException {
    log.info("Looking for NetworkServiceRecord with id: " + nsrId);
    // check if the nsr status is in ACTIVE
    if (!nsrRepository.existsByIdAndProjectIdAndStatus(nsrId, projectId, Status.ACTIVE))
      throw new BadRequestException("NSR is not in ACTIVE status");
    NetworkServiceRecord nsr = nsrRepository.findFirstByIdAndProjectId(nsrId, projectId);

    //Check if the VNFD exists
    VirtualNetworkFunctionDescriptor vnfd =
        vnfdRepository.findFirstByIdAndProjectId(vnfdId, projectId);
    if (vnfd == null) {
      throw new NotFoundException(
          "VNFD with id " + vnfdId + " was not found in the project with id " + projectId);
    }
    DeployNSRBody body = getDeployNSRBody(keys, vduVimInstances, configurations, projectId);
    return scaleOutNsr(nsr, vnfd, projectId, body, monitoringIp);
  }

  @Override
  public VirtualNetworkFunctionRecord restartVnfr(
      NetworkServiceRecord nsr, String vnfrId, String imageName, String projectId)
      throws NotFoundException, IOException, BadRequestException, VimException, PluginException,
          ExecutionException, InterruptedException, BadFormatException {

    // check if the nsr status is in ACTIVE
    if (!nsrRepository.existsByIdAndProjectIdAndStatus(nsr.getId(), projectId, Status.ACTIVE))
      throw new BadRequestException("NSR is not in ACTIVE status");

    // Check if the vnfr exists and belongs to the nsr
    VirtualNetworkFunctionRecord vnfr =
        vnfrRepository.findByIdAndParent_ns_idAndProjectId(vnfrId, nsr.getId(), projectId);
    if (vnfr == null)
      throw new NotFoundException(
          "Not found VNFR with id: " + vnfrId + " in NSR with id: " + nsr.getId());

    // check if the image is available in the vim
    if (imageName != null && !imageName.isEmpty()) {
      for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
        boolean imageNameSpecifiedDiffers = !vdu.getVm_image().iterator().next().equals(imageName);
        if (imageNameSpecifiedDiffers) {
          for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
            BaseVimInstance vimInstance =
                vimInstanceRepository.findFirstByIdAndProjectId(
                    vnfcInstance.getVim_id(), projectId);
            if (!vimInstance.getType().equals("test")) {
              vimManagement.refresh(vimInstance, false).get();
              if (VimInstanceUtils.findActiveImagesByName(vimInstance, imageName).size() == 0)
                throw new NotFoundException(
                    "Not found image " + imageName + " in vim: " + vimInstance.getName());
            }
          }
          // the image specified replaces the old one
          vdu.getVm_image().clear();
          vdu.getVm_image().add(imageName);
        }
      }
    }

    // set nsr status to null since the virtual resources (virtual machines) are going
    // to be rebuilt. Setting the status not to ACTIVE also prevent other actions to be performed
    nsrRepository.setStatus(nsr.getId(), Status.NULL);

    // rebuild VNF, namely rebuild all the vnfc instances in every vdu
    try {
      for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
        resourceManagement.operate(vdu, "rebuild").get();
      }
    } catch (PluginException | VimException | ExecutionException | InterruptedException e) {
      nsrRepository.setStatus(nsr.getId(), Status.ERROR);
      throw e;
    }
    // save image in the vnfr
    // it is done after the rebuild in case exceptions make the rebuild fail
    vnfr = vnfrRepository.save(vnfr);

    // execute restart vnfr
    vnfmManager.restartVnfr(vnfr);

    return vnfr;
  }

  private NetworkServiceRecord scaleOutNsr(
      NetworkServiceRecord networkServiceRecord,
      VirtualNetworkFunctionDescriptor vnfd,
      String projectId,
      DeployNSRBody body,
      String monitoringIp)
      throws BadRequestException, NotFoundException, InterruptedException, BadFormatException,
          ExecutionException, CyclicDependenciesException, NetworkServiceIntegrityException {
    Map<String, Set<String>> vduVimInstances = new HashMap<>();
    log.info("Scaling NetworkServiceRecord: " + networkServiceRecord.getName());
    log.trace("Scaling NetworkServiceRecord: " + networkServiceRecord);
    log.info("VNFD to add is: " + vnfd.getName());

    log.info("Checking if all vnfm are registered and active");
    Iterable<VnfmManagerEndpoint> endpoints = vnfmManagerEndpointRepository.findAll();
    nsdUtils.checkEndpoint(vnfd.getEndpoint(), endpoints);

    NetworkServiceDescriptor nsd =
        nsdRepository.findFirstByIdAndProjectId(
            networkServiceRecord.getDescriptor_reference(), projectId);

    boolean savedNsrSuccessfully = false;
    int attempt = 0;
    // this while loop is necessary, because while creating the NSR also a VIM might be changed (newly created networks).
    // then saving the NSR might produce OptimisticLockingFailureExceptions.
    while (!savedNsrSuccessfully) {
      // Save the keys in the NSR
      networkServiceRecord.setStatus(Status.SCALING);
      networkServiceRecord.setTask("Scaling out");
      if (body != null && body.getKeys() != null && !body.getKeys().isEmpty()) {
        for (Key key : body.getKeys()) {
          networkServiceRecord.getKeyNames().add(key.getName());
        }
      }

      vnfd.setCreatedAt(NSRUtils.getFormat().format(new Date()));
      vnfd.setUpdatedAt(NSRUtils.getFormat().format(new Date()));

      for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
        // get the vim instance names on where to deploy the vdu
        log.debug("Checking vim instance support");
        Set<String> instanceNames = getRuntimeDeploymentInfo(body, vdu);
        instanceNames = checkIfVimAreSupportedByPackage(vnfd, instanceNames);
        vduVimInstances.put(vdu.getId(), instanceNames);

        // vduVimInstances now contains all possible vim instance names for a specific vdu
        for (String vimInstanceName : instanceNames) {

          String name =
              vimInstanceName.contains(":") ? vimInstanceName.split(":")[0] : vimInstanceName;
          BaseVimInstance vimInstance =
              vimInstanceRepository.findByProjectIdAndName(vdu.getProjectId(), name);

          if (vimInstance == null) {
            throw new NotFoundException("Not found VIM instance: " + vimInstanceName);
          }
        }
      }

      // TODO it better: Check if the chosen VIM has ENOUGH Resources for deployment
      //checkQuotaForNS();

      //get the set of all vnfDependencies
      Set<VNFDependency> vnfDependencySet = nsdUtils.getVNFDependenciesFromRequires(vnfd, nsd);
      vnfDependencySet.addAll(new HashSet<>(nsd.getVnf_dependency()));

      //get the set of all VNFD
      Set<VirtualNetworkFunctionDescriptor> vnfdSet = new HashSet<>(nsd.getVnfd());
      vnfdSet.add(vnfd);

      //set the dependencies
      NSRUtils.setDependencies(vnfdSet, vnfDependencySet, networkServiceRecord);

      vnfd.setProjectId(projectId);
      try {
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        savedNsrSuccessfully = true;
        log.debug(
            "Persisted NSR "
                + networkServiceRecord.getName()
                + ". Got id: "
                + networkServiceRecord.getId());
      } catch (OptimisticLockingFailureException e) {
        if (attempt >= 3) {
          log.error(
              "After 4 attempts there is still an OptimisticLockingFailureException when creating the NSR. Stop trying.");
          throw e;
        }
        log.warn("OptimisticLockingFailureException while creating the NSR. We will try it again.");
        savedNsrSuccessfully = false;
        attempt++;
      }
    }

    // If the new VNF is a target, we need to fill the dependencies of its sources
    for (VNFRecordDependency vnfrd : networkServiceRecord.getVnf_dependency()) {
      if (vnfrd.getTarget().equals(vnfd.getName())) {
        for (String vnfrSourceName : vnfrd.getIdType().keySet()) {
          VirtualNetworkFunctionRecord vnfrSource = getVNFR(networkServiceRecord, vnfrSourceName);
          dependencyManagement.fillDependecyParameters(vnfrSource);
        }
      }
    }

    checkConfigParameter(vnfd, body);

    vnfmManager.addVnfr(networkServiceRecord, vnfd, body, vduVimInstances, monitoringIp);
    log.debug("Returning NSR " + networkServiceRecord.getName());
    return networkServiceRecord;
  }

  @Override
  public NetworkServiceRecord onboard(
      NetworkServiceDescriptor networkServiceDescriptor,
      String projectId,
      List keys,
      Map vduVimInstances,
      Map configurations,
      String monitoringIp)
      throws VimException, NotFoundException, PluginException, BadRequestException, IOException,
          AlreadyExistingException, BadFormatException, ExecutionException, InterruptedException {
    networkServiceDescriptor.setProjectId(projectId);
    DeployNSRBody body = getDeployNSRBody(keys, vduVimInstances, configurations, projectId);
    return deployNSR(networkServiceDescriptor, projectId, body, monitoringIp);
  }

  private DeployNSRBody getDeployNSRBody(
      List keys, Map vduVimInstances, Map configurations, String projectId) {
    DeployNSRBody body = new DeployNSRBody();
    if (vduVimInstances == null) {
      body.setVduVimInstances(new HashMap<>());
    } else {
      body.setVduVimInstances(vduVimInstances);
    }
    if (configurations == null) {
      body.setConfigurations(new HashMap());
    } else {
      body.setConfigurations(configurations);
    }
    if (keys == null) {
      body.setKeys(null);
    } else {
      Set<Key> keys1 = new LinkedHashSet<>();
      for (Object k : keys) {
        log.debug("Looking for keyname: " + k);
        keys1.add(keyRepository.findKey(projectId, (String) k));
      }
      body.setKeys(keys1);
      log.debug("Found keys: " + body.getKeys());
    }
    return body;
  }

  public void deleteVNFRecord(String idNsr, String idVnf, String projectId)
      throws NotFoundException {
    //TODO the logic of this request for the moment deletes only the VNFR from the DB, need to be removed from the
    // running NetworkServiceRecord
    VirtualNetworkFunctionRecord vnfr = vnfrRepository.findOne(idVnf);
    if (vnfr == null) {
      throw new NotFoundException("Not found VNFR with ID: " + idVnf);
    }
    if (!vnfr.getParent_ns_id().equals(idNsr)) {
      throw new NotFoundException("Not found VNFR " + idVnf + " in the given NSR " + idNsr);
    }
    if (!vnfr.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException("VNFR not contained in the chosen project.");
    }
    nsrRepository.deleteVNFRecord(idNsr, idVnf);
    NetworkServiceRecord nsr = query(idNsr, projectId);
    if (nsr != null) {
      for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : nsr.getVnfr()) {
        if (nsr.getStatus().ordinal() > virtualNetworkFunctionRecord.getStatus().ordinal()) {
          nsr.setStatus(vnfr.getStatus());
        }
      }
      nsrRepository.saveCascade(nsr);
    } else {
      log.warn("Parent NS does not exist anymore...");
    }
  }

  /**
   * Returns the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
   *
   * @param idNsr of Nsr
   * @param idVnf of VirtualNetworkFunctionRecord
   * @param projectId the current projectId
   * @return VirtualNetworkFunctionRecord selected
   */
  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
      String idNsr, String idVnf, String projectId) throws NotFoundException {
    NetworkServiceRecord networkServiceRecord = query(idNsr, projectId);
    for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord :
        networkServiceRecord.getVnfr()) {
      if (virtualNetworkFunctionRecord.getId().equals(idVnf)) {
        return virtualNetworkFunctionRecord;
      }
    }
    throw new NotFoundException("VNFR with ID " + idVnf + " was not found in NSR with ID " + idNsr);
  }

  /**
   * Removes a VNFDependency from an NSR.
   *
   * @param idNsr ID of the NSR
   * @param idVnfd ID of the VNFDependency
   * @param projectId the current projectId
   */
  @Override
  public void deleteVNFDependency(String idNsr, String idVnfd, String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsr = query(idNsr, projectId);
    VNFRecordDependency vnfDependency = null;
    for (VNFRecordDependency vnfdep : nsr.getVnf_dependency()) {
      if (vnfdep.getId().equals(idVnfd)) {
        vnfDependency = vnfdep;
      }
    }
    if (vnfDependency == null) {
      throw new NotFoundException(
          "No VNFDependency with ID " + idVnfd + " found in NSR with ID " + idNsr);
    }

    nsr.getVnf_dependency().remove(vnfDependency);
    nsrRepository.saveCascade(nsr);
  }

  @Override
  public void addVNFCInstance(
      String id,
      String idVnf,
      VNFComponent component,
      String projectId,
      List<String> vimInstanceNames)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.info("Adding new VNFCInstance to VNFR with id: " + idVnf);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id, projectId);
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }

    List<VirtualDeploymentUnit> vdusFound = new LinkedList<>();
    VirtualDeploymentUnit virtualDeploymentUnit = null;

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      if (vdu.getProjectId() != null && vdu.getProjectId().equals(projectId)) {
        vdusFound.add(vdu);
      }
    }

    if (vdusFound.size() == 0) {
      throw new NotFoundException("No VirtualDeploymentUnit found");
    }

    for (VirtualDeploymentUnit vdu : vdusFound) {
      if (vdu.getScale_in_out() == vdu.getVnfc_instance().size()) {
        continue;
      }
      virtualDeploymentUnit = vdu;
      break;
    }
    if (virtualDeploymentUnit == null) {
      throw new NotFoundException(
          "All VirtualDeploymentUnits have reached their maximum number of VNFCInstances");
    }

    if (vimInstanceNames == null || vimInstanceNames.isEmpty()) {
      vimInstanceNames = new ArrayList<>();
      for (BaseVimInstance vimInstance : vimInstanceRepository.findByProjectId(projectId)) {
        vimInstanceNames.add(vimInstance.getName());
      }
    } else {
      List<String> finalVimInstanceNames = new ArrayList<>();
      vimInstanceNames.forEach(
          n -> {
            if (n.contains(":")) {
              finalVimInstanceNames.add(n.split(Pattern.quote(":"))[0]);
            } else {
              finalVimInstanceNames.add(n);
            }
          });
      if (vimInstanceRepository
          .findByProjectId(projectId)
          .stream()
          .noneMatch(v -> finalVimInstanceNames.contains(v.getName()))) {
        log.error("VimInstance names passed not found: " + vimInstanceNames);
        throw new NotFoundException("VimInstance names passed not found: " + vimInstanceNames);
      }
    }
    log.debug(
        "A new VNFCInstance will be added to the VDU with ID " + virtualDeploymentUnit.getId());

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.saveCascade(networkServiceRecord);
    scaleOUT(
        networkServiceRecord,
        virtualNetworkFunctionRecord,
        virtualDeploymentUnit,
        component,
        "",
        vimInstanceNames);
  }

  @Override
  public void addVNFCInstance(
      String id,
      String idVnf,
      String idVdu,
      VNFComponent component,
      String mode,
      String projectId,
      List<String> vimInstanceNames)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.info("Adding new VNFCInstance to VNFR with id " + idVnf + " and VDU with id " + idVdu);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id, projectId);
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    if (virtualDeploymentUnit.getScale_in_out()
        == virtualDeploymentUnit.getVnfc_instance().size()) {
      throw new WrongStatusException(
          "The VirtualDeploymentUnit chosen has reached the maximum number of VNFCInstance");
    }

    Set<String> names = new HashSet<>();
    for (BaseVimInstance vimInstance : vimInstanceRepository.findByProjectId(projectId)) {
      names.add(vimInstance.getName());
    }
    if (vimInstanceNames == null || vimInstanceNames.isEmpty()) {
      vimInstanceNames = new ArrayList<>();
      for (BaseVimInstance vimInstance : vimInstanceRepository.findByProjectId(projectId)) {
        vimInstanceNames.add(vimInstance.getName());
      }
    }
    names.retainAll(vimInstanceNames);
    if (names.size() == 0) {
      log.error("VimInstance names passed not found");
      throw new NotFoundException("VimInstance names passed not found");
    }
    log.debug(
        "A new VNFCInstance will be added to the VDU with id " + virtualDeploymentUnit.getId());

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.saveCascade(networkServiceRecord);
    scaleOUT(
        networkServiceRecord,
        virtualNetworkFunctionRecord,
        virtualDeploymentUnit,
        component,
        mode,
        vimInstanceNames);
  }

  private void scaleOUT(
      NetworkServiceRecord networkServiceRecord,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VirtualDeploymentUnit virtualDeploymentUnit,
      VNFComponent component,
      String mode,
      List<String> vimInstanceNames)
      throws BadFormatException, NotFoundException {

    networkServiceRecord.setTask("Scaling out");
    List<String> componentNetworks = new ArrayList<>();

    if (component.getConnection_point() == null || component.getConnection_point().isEmpty()) {
      throw new BadFormatException("No connection points were passed for scaling out.");
    }

    for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
      if (connectionPoint.getVirtual_link_reference() == null
          || connectionPoint.getVirtual_link_reference().equals("")) {
        throw new BadFormatException("Connection points have to contain a virtual link reference.");
      }
      componentNetworks.add(connectionPoint.getVirtual_link_reference());
    }

    List<String> vnfrNetworks = new ArrayList<>();

    for (InternalVirtualLink virtualLink : virtualNetworkFunctionRecord.getVirtual_link()) {
      vnfrNetworks.add(virtualLink.getName());
    }

    for (String virtualLinkReference : componentNetworks) {
      if (!vnfrNetworks.contains(virtualLinkReference)) {
        throw new BadFormatException(
            "The virtual link reference "
                + virtualLinkReference
                + " does not exist in the VNFR "
                + virtualNetworkFunctionRecord.getName()
                + ". It has to be one of the "
                + "following: "
                + String.join(", ", vnfrNetworks));
      }
    }

    networkServiceRecord
        .getVlr()
        .forEach(
            vlr ->
                component
                    .getConnection_point()
                    .forEach(
                        cp -> {
                          if (cp.getVirtual_link_reference().equals(vlr.getName())) {
                            cp.setVirtual_link_reference_id(vlr.getExtId());
                          }
                        }));

    log.info(
        "Adding VNFComponent to VirtualNetworkFunctionRecord "
            + virtualNetworkFunctionRecord.getName());

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      if (vdu.getId().equals(virtualDeploymentUnit.getId())) {
        vdu.getVnfc().add(component);
      }
    }

    vnfcRepository.save(component);
    nsrRepository.saveCascade(networkServiceRecord);
    log.debug("new VNFComponent is " + component);

    VNFRecordDependency dependencyTarget =
        dependencyManagement.getDependencyForAVNFRecordTarget(virtualNetworkFunctionRecord);

    log.debug("Found Dependency: " + dependencyTarget);

    try {
      vnfmManager
          .addVnfc(
              virtualNetworkFunctionRecord, component, dependencyTarget, mode, vimInstanceNames)
          .get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deleteVNFCInstance(String id, String idVnf, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException, BadFormatException {
    log.info("Removing VNFCInstance from VNFR with id: " + idVnf);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id, projectId);
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }

    List<VirtualDeploymentUnit> vdusFound = new LinkedList<>();
    VirtualDeploymentUnit virtualDeploymentUnit = null;

    for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
      if (vdu.getProjectId() != null && vdu.getProjectId().equals(projectId)) {
        vdusFound.add(vdu);
      }
    }

    if (vdusFound.size() == 0) {
      throw new NotFoundException(
          "No VirtualDeploymentUnit found in VNFR " + virtualNetworkFunctionRecord.getName());
    }

    for (VirtualDeploymentUnit vdu : vdusFound) {
      if (vdu.getVnfc_instance().size() > 1) {
        virtualDeploymentUnit = vdu;
        break;
      }
    }

    if (virtualDeploymentUnit == null) {
      throw new NotFoundException(
          "All VirtualDeploymentUnits have reached their minimum number of VNFCInstances");
    }

    log.debug(
        "A VNFCInstance will be deleted from the VDU with id " + virtualDeploymentUnit.getId());

    VNFCInstance vnfcInstance = getVNFCInstance(virtualDeploymentUnit, null);

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.saveCascade(networkServiceRecord);
    scaleIn(
        networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, vnfcInstance);
  }

  @Override
  public void deleteVNFCInstance(String id, String idVnf, String idVdu, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException, BadFormatException {
    log.info("Removing VNFCInstance from VNFR with id: " + idVnf + " in vdu: " + idVdu);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id, projectId);
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }

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

    VNFCInstance vnfcInstance = getVNFCInstance(virtualDeploymentUnit, null);

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.saveCascade(networkServiceRecord);
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
          VimException, PluginException, BadFormatException {
    log.info(
        "Removing VNFCInstance with id: "
            + idVNFCI
            + " from VNFR with id: "
            + idVnf
            + " in vdu: "
            + idVdu);
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id, projectId);
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VDU not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    if (virtualDeploymentUnit.getVnfc_instance().size() == 1) {
      throw new WrongStatusException(
          "The VirtualDeploymentUnit chosen has reached the minimum number of VNFCInstance");
    }

    networkServiceRecord.setStatus(Status.SCALING);
    networkServiceRecord = nsrRepository.saveCascade(networkServiceRecord);
    scaleIn(
        networkServiceRecord,
        virtualNetworkFunctionRecord,
        virtualDeploymentUnit,
        getVNFCInstance(virtualDeploymentUnit, idVNFCI));
  }

  @Override
  public void startVNFCInstance(
      String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    startStopVNFCInstance(id, idVnf, idVdu, idVNFCI, projectId, Action.START);
  }

  @Override
  public void stopVNFCInstance(
      String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    startStopVNFCInstance(id, idVnf, idVdu, idVNFCI, projectId, Action.STOP);
  }

  private void startStopVNFCInstance(
      String id, String idVnf, String idVdu, String idVNFCI, String projectId, Action action)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInAnyState(id, projectId);
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VDU not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }

    VNFCInstance vnfcInstanceToStartStop = null;
    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
      log.debug(
          "VNFCInstance: (ID: "
              + vnfcInstance.getId()
              + " - HOSTNAME: "
              + vnfcInstance.getHostname()
              + " - STATE: "
              + vnfcInstance.getState()
              + ")");
      if (vnfcInstance.getId().equals(idVNFCI)) {
        vnfcInstanceToStartStop = vnfcInstance;
        switch (action) {
          case START:
            log.debug(
                "VNFCInstance to be started: "
                    + vnfcInstanceToStartStop.getId()
                    + " - "
                    + vnfcInstanceToStartStop.getHostname());
            break;
          case STOP:
            log.debug(
                "VNFCInstance to be stopped: "
                    + vnfcInstanceToStartStop.getId()
                    + " - "
                    + vnfcInstanceToStartStop.getHostname());
            break;
        }
      }
    }
    if (vnfcInstanceToStartStop == null) {
      switch (action) {
        case START:
          throw new NotFoundException("VNFCInstance to be started NOT FOUND");
        case STOP:
          throw new NotFoundException("VNFCInstance to be stopped NOT FOUND");
      }
    }

    OrVnfmStartStopMessage startStopMessage =
        new OrVnfmStartStopMessage(virtualNetworkFunctionRecord, vnfcInstanceToStartStop);
    switch (action) {
      case START:
        startStopMessage.setAction(Action.START);
        break;
      case STOP:
        startStopMessage.setAction(Action.STOP);
        break;
    }

    vnfStateHandler.sendMessageToVNFR(virtualNetworkFunctionRecord, startStopMessage);
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
      throws NotFoundException, WrongStatusException, BadFormatException, ExecutionException,
          InterruptedException {
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id, projectId);
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VDU not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    networkServiceRecord.setTask("Healing");
    VNFCInstance standByVNFCInstance = null;
    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
      log.debug("current vnfcinstance " + vnfcInstance + " in state" + vnfcInstance.getState());
      if (vnfcInstance.getState() != null && vnfcInstance.getState().equalsIgnoreCase(mode)) {
        standByVNFCInstance = vnfcInstance;
        log.debug("VNFComponentInstance in " + mode + " mode FOUND :" + standByVNFCInstance);
      }
      if (vnfcInstance.getId().equals(failedVnfcInstance.getId())) {
        vnfcInstance.setState("FAILED");
        log.debug(
            "The vnfcInstance: "
                + vnfcInstance.getHostname()
                + " is set to '"
                + vnfcInstance.getState()
                + "' state");
      }
    }
    if (standByVNFCInstance == null) {
      throw new NotFoundException(
          "No VNFCInstance in "
              + mode
              + " mode found, so switch to redundant VNFC is not "
              + "possibile");
    }

    //save the new state of the failedVnfcInstance
    nsrRepository.saveCascade(networkServiceRecord);

    OrVnfmHealVNFRequestMessage healMessage = new OrVnfmHealVNFRequestMessage();
    healMessage.setAction(Action.HEAL);
    healMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
    healMessage.setVnfcInstance(standByVNFCInstance);
    healMessage.setCause("switchToStandby");

    vnfStateHandler.sendMessageToVNFR(virtualNetworkFunctionRecord, healMessage);
  }

  /**
   * Returns the VNFCInstance with the passed ID from a specific VDU. If null is passed for the
   * VNFCInstance ID, the first VNFCInstance in the VDU is returned.
   *
   * @param virtualDeploymentUnit the VDU holding the VNFCinstance
   * @param idVNFCI the id of the VNFCInstance
   * @return the VNFCinstance
   * @throws NotFoundException if not found
   */
  private VNFCInstance getVNFCInstance(VirtualDeploymentUnit virtualDeploymentUnit, String idVNFCI)
      throws NotFoundException {

    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
      if (idVNFCI == null || idVNFCI.equals(vnfcInstance.getId())) {
        return vnfcInstance;
      }
    }

    if (idVNFCI != null) {
      throw new NotFoundException(
          "VNFCInstance with ID "
              + idVNFCI
              + " was not found in VDU with ID "
              + virtualDeploymentUnit.getId());
    } else {
      throw new NotFoundException(
          "No VNFCInstance found in VDU with ID " + virtualDeploymentUnit.getId());
    }
  }

  private void scaleIn(
      NetworkServiceRecord networkServiceRecord,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VirtualDeploymentUnit virtualDeploymentUnit,
      VNFCInstance vnfcInstance)
      throws NotFoundException, InterruptedException, ExecutionException, VimException,
          PluginException, BadFormatException {
    List<VNFRecordDependency> dependencySource =
        dependencyManagement.getDependencyForAVNFRecordSource(virtualNetworkFunctionRecord);

    networkServiceRecord.setTask("Scaling in");

    if (!dependencySource.isEmpty()) {
      for (VNFRecordDependency dependency : dependencySource) {
        //        List<String> paramsToRemove = new ArrayList<>();
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord1 :
            networkServiceRecord.getVnfr()) {
          if (virtualNetworkFunctionRecord1.getName().equals(dependency.getTarget())) {
            vnfmManager.removeVnfcDependency(virtualNetworkFunctionRecord1, vnfcInstance);
            for (Entry<String, VNFCDependencyParameters> parametersEntry :
                dependency.getVnfcParameters().entrySet()) {
              log.debug("Parameter: " + parametersEntry);
              if (parametersEntry.getValue() != null) {
                parametersEntry.getValue().getParameters().remove(vnfcInstance.getId());
              }
            }
          }
        }
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

    log.debug("Calculating NSR status");
    log.debug("Actual NSR stats is: " + networkServiceRecord.getStatus());
    for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
      boolean stopVNFR = true;
      for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
        for (VNFCInstance instanceInVNFR : vdu.getVnfc_instance()) {

          log.debug("VNFCInstance status is: " + instanceInVNFR.getState());
          // if vnfciStarted is not null then the START message received refers to the VNFCInstance
          if (instanceInVNFR.getState() != null) {
            if ((instanceInVNFR.getState().equalsIgnoreCase("active"))
                && (networkServiceRecord.getStatus().ordinal() != Status.ERROR.ordinal())) {
              stopVNFR = false;
              break;
            }
          }
        }
      }
      if (stopVNFR) {
        virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
        break;
      }
    }

    for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
      if (vnfr.getStatus().ordinal() == Status.INACTIVE.ordinal()) {
        networkServiceRecord.setStatus(Status.INACTIVE);
        break;
      }
    }

    ApplicationEventNFVO event =
        new ApplicationEventNFVO(
            Action.SCALE_IN,
            virtualNetworkFunctionRecord,
            virtualNetworkFunctionRecord.getProjectId());
    EventNFVO eventNFVO = new EventNFVO(this);
    eventNFVO.setEventNFVO(event);
    log.trace("Publishing event: " + event);
    publisher.dispatchEvent(eventNFVO);

    if (networkServiceRecord.getStatus().ordinal() == Status.SCALING.ordinal()) {
      networkServiceRecord.setStatus(Status.ACTIVE);
      networkServiceRecord.setTask("Scaled in");
    }
    nsrRepository.saveCascade(networkServiceRecord);
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
    if (virtualDeploymentUnit == null) {
      throw new NotFoundException("No VirtualDeploymentUnit found with id " + idVdu);
    }
    return virtualDeploymentUnit;
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
    if (virtualNetworkFunctionRecord == null) {
      throw new NotFoundException("No VirtualNetworkFunctionRecord found with id " + idVnf);
    }
    return virtualNetworkFunctionRecord;
  }

  private synchronized NetworkServiceRecord getNetworkServiceRecordInAnyState(
      String id, String projectId) throws NotFoundException {
    NetworkServiceRecord networkServiceRecord =
        nsrRepository.findFirstByIdAndProjectId(id, projectId);
    if (networkServiceRecord == null) {
      throw new NotFoundException("No NetworkServiceRecord found with ID " + id);
    }

    return networkServiceRecord;
  }

  private synchronized NetworkServiceRecord getNetworkServiceRecordInActiveState(
      String id, String projectId) throws NotFoundException, WrongStatusException {
    NetworkServiceRecord networkServiceRecord =
        nsrRepository.findFirstByIdAndProjectId(id, projectId);
    if (networkServiceRecord == null) {
      throw new NotFoundException("No NetworkServiceRecord found with ID " + id);
    }

    if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal()) {
      throw new WrongStatusException("NetworkServiceDescriptor must be in ACTIVE state");
    }

    return networkServiceRecord;
  }

  private NetworkServiceRecord deployNSR(
      NetworkServiceDescriptor networkServiceDescriptor,
      String projectID,
      DeployNSRBody body,
      String monitoringIp)
      throws NotFoundException, VimException, BadRequestException, BadFormatException,
          ExecutionException, InterruptedException {
    log.trace("Fetched NetworkServiceDescriptor: " + networkServiceDescriptor);

    log.debug("VNFD are: ");
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {
      log.debug("\t" + virtualNetworkFunctionDescriptor.getName());
    }

    log.info("Checking if all vnfm are registered and active");
    Iterable<VnfmManagerEndpoint> endpoints = vnfmManagerEndpointRepository.findAll();
    nsdUtils.checkEndpoint(networkServiceDescriptor, endpoints);

    Map<String, Set<String>> vduVimInstances = new HashMap<>();
    NetworkServiceRecord networkServiceRecord = null;
    boolean savedNsrSuccessfully = false;
    int attempt = 0;
    // this while loop is necessary, because while creating the NSR also a VIM might be changed (newly created
    // networks).
    // then saving the NSR might produce OptimisticLockingFailureExceptions.
    while (!savedNsrSuccessfully) {
      networkServiceRecord = NSRUtils.createNetworkServiceRecord(networkServiceDescriptor);
      if (body != null && body.getKeys() != null && !body.getKeys().isEmpty()) {
        for (Key key : body.getKeys()) {
          networkServiceRecord.getKeyNames().add(key.getName());
        }
      }
      log.trace("Creating " + networkServiceRecord);

      for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
          networkServiceDescriptor.getVnfd()) {

        virtualNetworkFunctionDescriptor.setCreatedAt(NSRUtils.getFormat().format(new Date()));
        virtualNetworkFunctionDescriptor.setUpdatedAt(NSRUtils.getFormat().format(new Date()));

        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionDescriptor.getVdu()) {
          Set<String> instanceNames = getRuntimeDeploymentInfo(body, vdu);
          log.debug("Checking vim instance support");
          instanceNames =
              checkIfVimAreSupportedByPackage(virtualNetworkFunctionDescriptor, instanceNames);
          vduVimInstances.put(vdu.getId(), instanceNames);
          // vduVimInstances now contains all possible vim instance names for a specific vdu
          for (String vimInstanceName : instanceNames) {

            String name =
                vimInstanceName.contains(":") ? vimInstanceName.split(":")[0] : vimInstanceName;
            BaseVimInstance vimInstance =
                vimInstanceRepository.findByProjectIdAndName(vdu.getProjectId(), name);

            if (vimInstance == null) {
              throw new NotFoundException("Not found VIM instance: " + vimInstanceName);
            }
          }
        }
      }

      // TODO it better: Check if the chosen VIM has ENOUGH Resources for deployment
      checkQuotaForNS(networkServiceDescriptor);

      NSRUtils.setDependencies(networkServiceDescriptor, networkServiceRecord);

      networkServiceRecord.setProjectId(projectID);
      try {
        networkServiceRecord = nsrRepository.saveCascade(networkServiceRecord);
        savedNsrSuccessfully = true;
        log.debug(
            "Persisted NSR "
                + networkServiceRecord.getName()
                + ". Got id: "
                + networkServiceRecord.getId());
      } catch (OptimisticLockingFailureException e) {
        if (attempt >= 3) {
          log.error(
              "After 4 attempts there is still an OptimisticLockingFailureException when creating the NSR. Stop trying.");
          throw e;
        }
        log.warn("OptimisticLockingFailureException while creating the NSR. We will try it again.");
        savedNsrSuccessfully = false;
        attempt++;
      }
    }

    checkConfigParameter(networkServiceDescriptor, body);

    fillDeploymentTimeIPs(networkServiceDescriptor, body, vduVimInstances);

    checkSshInfo(networkServiceDescriptor, body);
    vnfmManager.deploy(
        networkServiceDescriptor, networkServiceRecord, body, vduVimInstances, monitoringIp);
    log.debug("Returning NSR " + networkServiceRecord.getName());
    return networkServiceRecord;
  }

  private void checkSshInfo(NetworkServiceDescriptor nsd, DeployNSRBody body)
      throws NotFoundException {
    for (VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()) {
      if (body.getConfigurations().get(vnfd.getName()) == null) {
        continue;
      }
      boolean isSshUsernameProvided = false;
      boolean isSshPasswordProvided = false;
      for (ConfigurationParameter passedConfigurationParameter :
          body.getConfigurations().get(vnfd.getName()).getConfigurationParameters()) {
        if (passedConfigurationParameter.getConfKey().equalsIgnoreCase("ssh_username")
            && passedConfigurationParameter.getValue() != null
            && !passedConfigurationParameter.getValue().isEmpty()) {
          isSshUsernameProvided = true;
        }
        if (passedConfigurationParameter.getConfKey().equals("ssh_password")
            && passedConfigurationParameter.getValue() != null
            && !passedConfigurationParameter.getValue().isEmpty()) {
          isSshPasswordProvided = true;
        }
      }
      // Throw an exception if only one of them is provided.
      // - username without password is not allowed
      // - password without username is not allowed
      // - username and password is allowed
      // - no username and no password is allowed because this configuration can be done in the configuration file of
      //    the Fixed-host VNFM.
      if (isSshPasswordProvided != isSshUsernameProvided) {
        throw new NotFoundException(
            "Provide both ssh_username and ssh_password for the vnfd: " + vnfd.getName());
      }
    }
  }

  private void fillDeploymentTimeIPs(
      NetworkServiceDescriptor networkServiceDescriptor,
      DeployNSRBody body,
      Map<String, Set<String>> vduVimInstances)
      throws NotFoundException {
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {

      if (!virtualNetworkFunctionDescriptor.getEndpoint().equals("fixed-host")) {
        continue;
      }

      // Here we assume the VNFD contains only one VDU and one VNF component.
      VirtualDeploymentUnit vdu = virtualNetworkFunctionDescriptor.getVdu().iterator().next();
      VNFComponent vnfComponent = vdu.getVnfc().iterator().next();

      boolean isFixedHostVimUsed = false;
      for (BaseVimInstance vimInstance :
          vimInstanceRepository.findByProjectId(virtualNetworkFunctionDescriptor.getProjectId())) {
        if (vduVimInstances.get(vdu.getId()).contains(vimInstance.getName())
            && vimInstance.getType().equals("fixed-host")) {
          isFixedHostVimUsed = true;
        }
      }

      for (ConfigurationParameter passedConfigurationParameter :
          body.getConfigurations()
              .get(virtualNetworkFunctionDescriptor.getName())
              .getConfigurationParameters()) {
        if (passedConfigurationParameter.getConfKey().startsWith("ssh_")
            && passedConfigurationParameter.getConfKey().endsWith("_ip")) {
          if (passedConfigurationParameter.getValue().equals("random") && isFixedHostVimUsed) {
            throw new NotFoundException(
                "Specify the parameter "
                    + passedConfigurationParameter.getConfKey()
                    + " of the vnfd "
                    + virtualNetworkFunctionDescriptor.getName()
                    + " with a valid IP");
          }
          for (VNFDConnectionPoint vnfdConnectionPoint : vnfComponent.getConnection_point()) {
            if (passedConfigurationParameter
                .getConfKey()
                .contains(vnfdConnectionPoint.getVirtual_link_reference())) {
              log.debug(
                  "VNF: "
                      + virtualNetworkFunctionDescriptor.getName()
                      + ", setting ip: "
                      + passedConfigurationParameter.getValue()
                      + " to cp: "
                      + vnfdConnectionPoint.getVirtual_link_reference());
              vnfdConnectionPoint.setFloatingIp(passedConfigurationParameter.getValue());
              break;
            }
          }
        }
      }
    }
  }

  private void checkConfigParameter(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, DeployNSRBody body) {
    for (String vnfrName : body.getConfigurations().keySet()) {
      if (virtualNetworkFunctionDescriptor.getName() != null) {
        if (virtualNetworkFunctionDescriptor.getName().equals(vnfrName)) {
          if (virtualNetworkFunctionDescriptor.getConfigurations() != null) {
            if (body.getConfigurations().get(vnfrName).getName() != null
                && !body.getConfigurations().get(vnfrName).getName().isEmpty()) {
              virtualNetworkFunctionDescriptor
                  .getConfigurations()
                  .setName(body.getConfigurations().get(vnfrName).getName());
            }
            virtualNetworkFunctionDescriptor
                .getConfigurations()
                .getConfigurationParameters()
                .addAll(body.getConfigurations().get(vnfrName).getConfigurationParameters());
          } else {
            virtualNetworkFunctionDescriptor.setConfigurations(
                body.getConfigurations().get(vnfrName));
          }
        }
      } else {
        log.warn(
            "Not found name for VNFD "
                + virtualNetworkFunctionDescriptor.getId()
                + ". Cannot set configuration parameters");
      }
    }
  }

  private void checkConfigParameter(
      NetworkServiceDescriptor networkServiceDescriptor, DeployNSRBody body) {
    if (networkServiceDescriptor.getVnfd() != null) {
      for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
          networkServiceDescriptor.getVnfd()) {
        for (String vnfrName : body.getConfigurations().keySet()) {
          if (virtualNetworkFunctionDescriptor.getName() != null) {
            if (virtualNetworkFunctionDescriptor.getName().equals(vnfrName)) {
              if (virtualNetworkFunctionDescriptor.getConfigurations() != null) {
                if (body.getConfigurations().get(vnfrName).getName() != null
                    && !body.getConfigurations().get(vnfrName).getName().isEmpty()) {
                  virtualNetworkFunctionDescriptor
                      .getConfigurations()
                      .setName(body.getConfigurations().get(vnfrName).getName());
                }
                for (ConfigurationParameter passedConfigurationParameter :
                    body.getConfigurations().get(vnfrName).getConfigurationParameters()) {
                  boolean isExisting = false;
                  for (ConfigurationParameter configurationParameter :
                      virtualNetworkFunctionDescriptor
                          .getConfigurations()
                          .getConfigurationParameters()) {
                    if (configurationParameter
                        .getConfKey()
                        .equals(passedConfigurationParameter.getConfKey())) {
                      configurationParameter.setValue(passedConfigurationParameter.getValue());
                      if (passedConfigurationParameter.getDescription() != null
                          && !passedConfigurationParameter.getDescription().isEmpty()) {
                        configurationParameter.setDescription(
                            passedConfigurationParameter.getDescription());
                      }
                      isExisting = true;
                      break;
                    }
                  }
                  if (!isExisting) {
                    virtualNetworkFunctionDescriptor
                        .getConfigurations()
                        .getConfigurationParameters()
                        .add(passedConfigurationParameter);
                  }
                }
              } else {
                virtualNetworkFunctionDescriptor.setConfigurations(
                    body.getConfigurations().get(vnfrName));
              }
            }
          } else {
            log.warn(
                "Not found name for VNFD "
                    + virtualNetworkFunctionDescriptor.getId()
                    + ". Cannot set configuration parameters");
          }
        }
      }
    }
  }

  private void checkQuotaForNS(NetworkServiceDescriptor networkServiceDescriptor)
      throws VimException {
    try {
      if (isQuotaCheckEnabled) {
        Map<BaseVimInstance, Quota> requirements = new HashMap<>();
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
          for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
            int floatingIpCount = 0;
            for (VNFComponent vnfComponent : vdu.getVnfc()) {
              for (VNFDConnectionPoint vnfdConnectionPoint : vnfComponent.getConnection_point()) {
                if (vnfdConnectionPoint.getFloatingIp() != null) {
                  floatingIpCount++;
                }
              }
            }
            for (String vimInstanceName : vdu.getVimInstanceName()) {
              BaseVimInstance vimInstance = null;
              for (BaseVimInstance vim :
                  vimInstanceRepository.findByProjectId(vnfd.getProjectId())) {
                if (vim.getName().equals(vimInstanceName)) {
                  vimInstance = vim;
                }
              }
              DeploymentFlavour df = null;
              String df_key = vnfd.getDeployment_flavour().iterator().next().getFlavour_key();
              if (vimInstance != null && vimInstance instanceof OpenstackVimInstance) {
                OpenstackVimInstance osVimInstance = (OpenstackVimInstance) vimInstance;
                for (DeploymentFlavour deploymentFlavour : osVimInstance.getFlavours()) {
                  // TODO: Should find a better solution for here and generic
                  if (deploymentFlavour.getFlavour_key().equals(df_key)) {
                    df = deploymentFlavour;
                  }
                }
                if (df == null) {
                  throw new NotFoundException(
                      "Deployment Flavour key: "
                          + df_key
                          + " not supported in VIM Instance: "
                          + vimInstance.getName());
                }
                if (!requirements.keySet().contains(vimInstance)) {
                  Quota quota = new Quota();
                  quota.setCores(df.getVcpus());
                  quota.setInstances(1);
                  quota.setRam(df.getRam());
                  quota.setFloatingIps(floatingIpCount);
                  requirements.put(vimInstance, quota);
                } else {
                  requirements
                      .get(vimInstance)
                      .setCores(requirements.get(vimInstance).getCores() + df.getVcpus());
                  requirements
                      .get(vimInstance)
                      .setInstances(requirements.get(vimInstance).getInstances() + 1);
                  requirements
                      .get(vimInstance)
                      .setRam(requirements.get(vimInstance).getRam() + df.getRam());
                  requirements
                      .get(vimInstance)
                      .setFloatingIps(
                          requirements.get(vimInstance).getFloatingIps() + floatingIpCount);
                }
              }
            }
          }
        }

        for (BaseVimInstance vimInstance : requirements.keySet()) {
          Quota leftQuota = vimBroker.getLeftQuota(vimInstance);
          Quota neededQuota = requirements.get(vimInstance);
          log.info(
              "Needed Quota for VIM Instance:" + vimInstance.getName() + " is: " + neededQuota);
          if (leftQuota.getRam() < neededQuota.getRam()
              || leftQuota.getCores() < neededQuota.getCores()
              || leftQuota.getInstances() < neededQuota.getInstances()
              || leftQuota.getFloatingIps() < neededQuota.getFloatingIps()) {
            throw new VimException(
                "The VIM "
                    + vimInstance.getName()
                    + " does not have the "
                    + "needed resources to "
                    + "deploy all the VNFCs"
                    + " with the specified "
                    + "Deployment Flavours."
                    + "You should lower the"
                    + " Deployment Flavours"
                    + " or free up "
                    + "resources.");
          } else {
            log.info(
                "Resource check done: ",
                "Vim Instance has enough resources. Moving on with deployment.");
          }
        }
      } else {
        log.warn("Quota check is disabled... Please enable for comprehensive grant operation");
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.error(e.getMessage(), e);
      }
      if (failingQuotaCheckOnException) {
        String errorMsg =
            "Check Quota for NS threw an exception and operation will cancel deployment. For succeeding consider to "
                + "set 'nfvo.quota.check.failOnException' to false";
        log.error(errorMsg);
        throw new VimException(errorMsg, e);
      } else {
        log.warn(
            "Check Quota for NS threw an exception but operation will proceed. For failing consider to set 'nfvo"
                + ".quota.check.failOnException' to true");
      }
    }
  }

  private Set<String> checkIfVimAreSupportedByPackage(
      VirtualNetworkFunctionDescriptor vnfd, Set<String> instanceNames) throws BadRequestException {
    VNFPackage vnfPackage = vnfPackageRepository.findFirstById(vnfd.getVnfPackageLocation());
    if (vnfPackage == null
        || vnfPackage.getVimTypes() == null
        || vnfPackage.getVimTypes().size() == 0) {
      log.warn("VNFPackage does not provide supported VIM. I will skip the check!");
    } else {
      for (String vimInstanceName : instanceNames) {
        BaseVimInstance vimInstance;
        for (BaseVimInstance vi : vimInstanceRepository.findByProjectId(vnfd.getProjectId())) {
          if (vimInstanceName.equals(vi.getName())) {
            vimInstance = vi;
            log.debug("Found vim instance " + vimInstance.getName());
            log.debug(
                "Checking if "
                    + vimInstance.getType()
                    + " is contained in "
                    + vnfPackage.getVimTypes());
            if (!vnfPackage.getVimTypes().contains(vimInstance.getType())) {
              throw new org.openbaton.exceptions.BadRequestException(
                  "The Vim Instance chosen does not support the VNFD " + vnfd.getName());
            }
          }
        }
      }
    }
    if (instanceNames.size() == 0) {
      for (BaseVimInstance vimInstance :
          vimInstanceRepository.findByProjectId(vnfd.getProjectId())) {
        if (vnfPackage == null
            || vnfPackage.getVimTypes() == null
            || vnfPackage.getVimTypes().isEmpty()) {
          instanceNames.add(vimInstance.getName());
        } else {
          String type = vimInstance.getType();
          if (type.contains(".")) {
            type = type.split("\\.")[0];
          }
          if (vnfPackage.getVimTypes().contains(type)) {
            instanceNames.add(vimInstance.getName());
          }
        }
      }
    }

    if (instanceNames.size() == 0) {
      throw new org.openbaton.exceptions.BadRequestException(
          "No Vim Instance found for supporting the VNFD "
              + vnfd.getName()
              + " (looking for vim type: "
              + (vnfPackage != null ? vnfPackage.getVimTypes() : null)
              + ")");
    }
    log.debug("Vim Instances chosen are: " + instanceNames);
    return instanceNames;
  }

  // get vim instance names from the VNFD or the DeployNSRbody otherwise
  private Set<String> getRuntimeDeploymentInfo(DeployNSRBody body, VirtualDeploymentUnit vdu)
      throws NotFoundException {
    Set<String> instanceNames = null;

    if (body == null
        || body.getVduVimInstances() == null
        || body.getVduVimInstances().get(vdu.getName()) == null
        || body.getVduVimInstances().get(vdu.getName()).isEmpty()) {

      if (vdu.getVimInstanceName() == null || vdu.getVimInstanceName().size() == 0) {
        List<BaseVimInstance> vimInstances =
            vimInstanceRepository.findByProjectId(vdu.getProjectId());
        if (vimInstances.size() == 0)
          throw new NotFoundException("No VimInstance in the catalogue");
        else
          for (BaseVimInstance vimInstance : vimInstances) {
            instanceNames = new HashSet<>();
            instanceNames.add(vimInstance.getName());
          }
      } else {
        instanceNames = vdu.getVimInstanceName();
      }
    } else {
      instanceNames = body.getVduVimInstances().get(vdu.getName());
    }
    return instanceNames;
  }

  /**
   * Execute the update of a NSR
   *
   * @param newNsr
   * @param idNsr
   * @param projectId
   * @return
   * @throws NotFoundException
   */
  @Override
  public NetworkServiceRecord update(NetworkServiceRecord newNsr, String idNsr, String projectId)
      throws NotFoundException {
    // TODO not implemented yet
    log.warn("Updating NSRs is not yet implemented.");
    return query(idNsr, projectId);
  }

  /**
   * Execute the update of a Virtual Network Function Record (VNFR) (The UPDATE is intended as the
   * execution of the scripts associated to the lifecycle UPDATE by the VNF provider)
   *
   * @param nsrId
   * @param vnfrId
   * @param projectId
   * @return
   * @throws NotFoundException
   */
  @Override
  public VirtualNetworkFunctionRecord updateVnfr(String nsrId, String vnfrId, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    VirtualNetworkFunctionRecord vnfr =
        vnfrRepository.findByIdAndParent_ns_idAndProjectId(vnfrId, nsrId, projectId);
    log.info("Updating VNFR: " + vnfr.getName());
    vnfmManager.updateVnfr(nsrId, vnfrId, projectId);
    return vnfr;
  }

  /**
   * Execute the upgrade of a VNFR (The UPGRADE is intended as one of the following the rebuild of
   * the VNFR (all the VNFC Instances, if many) using a new OS image and/or new VNF scripts
   *
   * @param nsrId
   * @param vnfrId
   * @param projectId
   * @return
   * @throws NotFoundException
   */
  @Override
  public VirtualNetworkFunctionRecord upgradeVnfr(
      String nsrId, String vnfrId, String projectId, String upgradeVnfdId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException,
          IOException, BadRequestException, VimException, PluginException {

    NetworkServiceRecord nsr = nsrRepository.findFirstByIdAndProjectId(nsrId, projectId);

    VirtualNetworkFunctionRecord vnfr =
        vnfrRepository.findByIdAndParent_ns_idAndProjectId(vnfrId, nsrId, projectId);
    log.info("Upgrading VNFR: " + vnfr.getName() + " - " + vnfr.getId());

    VirtualNetworkFunctionDescriptor upgradeVnfd =
        vnfdRepository.findFirstByIdAndProjectId(upgradeVnfdId, projectId);
    String upgradeVnfPackageId = upgradeVnfd.getVnfPackageLocation();

    // Update VNFR references to new VNFD and VNF Package
    log.debug("Upgrading VNFR: " + vnfrId + " - Setting new VNFD reference: " + upgradeVnfdId);
    vnfr.setDescriptor_reference(upgradeVnfdId);
    log.debug(
        "Upgrading VNFR: "
            + vnfrId
            + " - Setting new VNFPackage reference: "
            + upgradeVnfPackageId);
    vnfr.setPackageId(upgradeVnfPackageId);
    log.debug(
        "Upgrading VNFR: " + vnfrId + " - Setting new VNFD version: " + upgradeVnfd.getVersion());
    vnfr.setVersion(upgradeVnfd.getVersion());

    // Use the first image found in the VDUs TODO: improve image choice for upgrade/rebuild
    String rebuildImageName = null;
    for (VirtualDeploymentUnit vdu : upgradeVnfd.getVdu()) {
      if (vdu.getVm_image().size() > 0) {
        rebuildImageName = vdu.getVm_image().iterator().next();
        log.debug("Upgrading VNFR: " + vnfrId + " - Using image: " + rebuildImageName);
        break;
      }
    }
    if (rebuildImageName == null)
      throw new NotFoundException("No images specified in new VNFD: " + upgradeVnfdId);

    // Initialise the context for the automatic trigger of the NSR restart
    vnfmManager.upgradeVnfr(nsrId, vnfr.getId(), projectId);

    // Rebuild the VNF using the upgraded VNF Descriptor and VNF Package and after the rebuild will be finished the
    // restart of the NSR will be automatically triggered
    restartVnfr(nsr, vnfrId, rebuildImageName, projectId);

    // save at the end in case there are no exceptions
    return vnfrRepository.save(vnfr);
  }

  @Override
  public Iterable<NetworkServiceRecord> query() {
    return nsrRepository.findAll();
  }

  /**
   * Triggers the execution of an {@link org.openbaton.catalogue.nfvo.Action} on a specific
   * VNFCInstance.
   *
   * <p>
   *
   * <p>Note: Currently only the HEAL action is supported.
   *
   * @param nfvMessage the NFVMessage to send
   * @param nsrId the NSR id
   * @param idVnf the VNFR id
   * @param idVdu the VDU id
   * @param idVNFCI the VNFCInstance id
   * @param projectId the current project id
   * @throws NotFoundException if something is not found
   */
  @Override
  public void executeAction(
      NFVMessage nfvMessage,
      String nsrId,
      String idVnf,
      String idVdu,
      String idVNFCI,
      String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {

    log.info("Executing action: " + nfvMessage.getAction() + " on VNF with id: " + idVnf);

    NetworkServiceRecord networkServiceRecord =
        nsrRepository.findFirstByIdAndProjectId(nsrId, projectId);
    if (networkServiceRecord == null) {
      throw new NotFoundException("No NSR found with ID " + nsrId);
    }
    VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
        getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);
    if (!virtualNetworkFunctionRecord.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VNFR not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    VirtualDeploymentUnit virtualDeploymentUnit =
        getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
    if (virtualDeploymentUnit.getProjectId() != null
        && !virtualDeploymentUnit.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "VDU not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    VNFCInstance vnfcInstance = getVNFCInstance(virtualDeploymentUnit, idVNFCI);
    switch (nfvMessage.getAction()) {
      case HEAL:
        // Note: when we get a HEAL message from the API, it contains only the cause (no vnfr or vnfcInstance).
        // Here the vnfr and the vnfcInstance are set into the message, since they are updated.
        VnfmOrHealedMessage VnfmOrHealVNFRequestMessage = (VnfmOrHealedMessage) nfvMessage;
        log.debug("Received Heal message: " + VnfmOrHealVNFRequestMessage);
        VnfmOrHealVNFRequestMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
        VnfmOrHealVNFRequestMessage.setVnfcInstance(vnfcInstance);
        vnfStateHandler.sendMessageToVNFR(
            virtualNetworkFunctionRecord, VnfmOrHealVNFRequestMessage);
        break;
    }
  }

  @Override
  public NetworkServiceRecord query(String id, String projectId) throws NotFoundException {
    log.trace("Id is: " + id);
    NetworkServiceRecord networkServiceRecord =
        nsrRepository.findFirstByIdAndProjectId(id, projectId);
    if (networkServiceRecord == null) {
      throw new NotFoundException("NetworkServiceRecord with ID " + id + " not found");
    }
    return networkServiceRecord;
  }

  @Override
  public void delete(String id, String projectId)
      throws NotFoundException, WrongStatusException, BadFormatException, ExecutionException,
          InterruptedException {
    log.info("Removing NSR with id: " + id);
    NetworkServiceRecord networkServiceRecord =
        nsrRepository.findFirstByIdAndProjectId(id, projectId);
    if (networkServiceRecord == null) {
      throw new NotFoundException("NetworkServiceRecord with ID " + id + " was not found");
    }

    if (!deleteInAllStatus) {
      if (networkServiceRecord.getStatus().ordinal() == Status.NULL.ordinal()) {
        throw new WrongStatusException(
            "The NetworkService "
                + networkServiceRecord.getId()
                + " is in the wrong state. ( Status= "
                + networkServiceRecord.getStatus()
                + " )");
      }
      if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal()
          && networkServiceRecord.getStatus().ordinal() != Status.ERROR.ordinal()) {
        throw new WrongStatusException(
            "The NetworkService "
                + networkServiceRecord.getId()
                + " is in the wrong state. ( Status= "
                + networkServiceRecord.getStatus()
                + " )");
      }
    }

    if (!networkServiceRecord.getVnfr().isEmpty()) {
      networkServiceRecord.setStatus(Status.TERMINATED); // TODO maybe terminating?
      for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
        vnfmManager.release(vnfr);
      }
      for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord :
          vnfrRepository.findByParentNsId(networkServiceRecord.getId())) {
        if (removeAfterTimeout) {
          VNFRTerminator terminator = new VNFRTerminator();
          terminator.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
          this.executor.submit(terminator);
        }
      }
    } else {
      nsrRepository.delete(networkServiceRecord.getId());
    }
  }

  private VirtualNetworkFunctionRecord getVNFR(NetworkServiceRecord nsr, String vnfrName)
      throws NotFoundException {
    for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
      if (vnfr.getName().equalsIgnoreCase(vnfrName)) {
        return vnfr;
      }
    }
    throw new NotFoundException(
        "No VNFR with name " + vnfrName + " in NSR " + nsr.getName() + " (" + nsr.getId() + ")");
  }

  private boolean isModifyHasBeenExecuted(VirtualNetworkFunctionRecord vnfr) {
    for (HistoryLifecycleEvent historyLifecycleEvent : vnfr.getLifecycle_event_history()) {
      if (historyLifecycleEvent.getEvent().equalsIgnoreCase("MODIFY")
          || historyLifecycleEvent.getEvent().equalsIgnoreCase("CONFIGURE")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void resume(String id, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInAnyState(id, projectId);

    if (!nsrRepository.existsByIdAndProjectIdAndStatus(id, projectId, Status.ERROR)) {
      log.warn("NSR is in not in ERROR status");
      throw new NotFoundException("NSR is not in ERROR status");
    }

    log.info("Resuming NSR with id: " + id);

    networkServiceRecord.setStatus(Status.RESUMING);

    // Resuming
    for (VirtualNetworkFunctionRecord failedVnfr : networkServiceRecord.getVnfr()) {

      // Send resume to VNFR in error
      if (failedVnfr.getStatus().ordinal() == (Status.ERROR.ordinal())) {
        failedVnfr.setStatus(Status.RESUMING);
        failedVnfr = vnfrRepository.save(failedVnfr);
        OrVnfmGenericMessage orVnfmGenericMessage = new OrVnfmGenericMessage();
        orVnfmGenericMessage.setVnfr(failedVnfr);
        log.debug("Setting VNFR Dependency for RESUMED VNFR");
        // Setting VNFR Dependency for RESUMED VNFR
        for (VNFRecordDependency vnfRecordDependency : networkServiceRecord.getVnf_dependency()) {
          if (vnfRecordDependency.getTarget().equals(failedVnfr.getName())) {
            log.debug(
                "Setting dependency to RESUMED VNFR: "
                    + vnfRecordDependency.getTarget()
                    + " == "
                    + failedVnfr.getName());
            orVnfmGenericMessage.setVnfrd(vnfRecordDependency);
          }
        }
        orVnfmGenericMessage.setAction(Action.RESUME);
        log.info("Sending resume message for VNFR: " + failedVnfr.getId());
        vnfStateHandler.sendMessageToVNFR(failedVnfr, orVnfmGenericMessage);
      }
    }

    for (VNFRecordDependency vnfrDependency : networkServiceRecord.getVnf_dependency()) {
      // Check for sources and target ready to have their dependencies resolved
      VirtualNetworkFunctionRecord vnfrTarget =
          getVNFR(networkServiceRecord, vnfrDependency.getTarget());
      if (vnfrTarget.getStatus().ordinal() == (Status.INITIALIZED.ordinal())) {

        List<VirtualNetworkFunctionRecord> resolvableVnfrSources = new ArrayList<>();
        boolean readyToResolve = true;
        for (String vnfrSourceName : vnfrDependency.getIdType().keySet()) {
          VirtualNetworkFunctionRecord vnfrSource = getVNFR(networkServiceRecord, vnfrSourceName);

          // Skipping dependency with a source in error
          if (vnfrSource.getStatus().ordinal() == (Status.ERROR.ordinal())
              && !isModifyHasBeenExecuted(vnfrSource)
              && vnfrTarget.getStatus().ordinal() < Status.INACTIVE.ordinal()) {
            log.info(
                "Not resolving dependencies for target: "
                    + vnfrTarget.getName()
                    + " - Its source: "
                    + vnfrSource.getName()
                    + " it is not ready (ERROR state)");
            readyToResolve = false;
          }
          // Resolving ready dependencies
          else {
            log.info(
                "Found resolvable dependency with source: "
                    + vnfrSource.getName()
                    + " and target: "
                    + vnfrTarget.getName());
            resolvableVnfrSources.add(vnfrSource);
          }
        }

        // Filling parameter for resolvable VNFR sources
        for (VirtualNetworkFunctionRecord resolvableVnfrSource : resolvableVnfrSources) {
          dependencyManagement.fillDependecyParameters(resolvableVnfrSource);
        }

        if (readyToResolve) {
          log.info("Sending MODIFY message to vnfr target: " + vnfrDependency.getTarget());

          OrVnfmGenericMessage orVnfmGenericMessage =
              new OrVnfmGenericMessage(vnfrTarget, Action.MODIFY);

          // Retrieve from VNFR Dependency Repository the dependency record for VNFR target with ready dependencies
          VNFRecordDependency resolvableVnfrDependency =
              vnfRecordDependencyRepository.findFirstById(vnfrDependency.getId());
          log.debug("Resolvable VNFR Dependency is: " + resolvableVnfrDependency);
          orVnfmGenericMessage.setVnfrd(resolvableVnfrDependency);
          vnfStateHandler.sendMessageToVNFR(vnfrTarget, orVnfmGenericMessage);
        } else {
          log.info("Not sending MODIFY message to vnfr target: " + vnfrDependency.getTarget());
        }
      }
    }
  }

  /**
   * This operation is used to execute a script on a specific Virtual Network Function Record during
   * runtime.
   */
  @Override
  public void executeScript(String idNsr, String idVnfr, String projectId, Script script)
      throws NotFoundException, InterruptedException, BadFormatException, ExecutionException {
    vnfmManager.executeScript(idVnfr, script);
  }

  class VNFRTerminator implements Runnable {

    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    void setVirtualNetworkFunctionRecord(
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
          virtualNetworkFunctionRecord
              .getVdu()
              .stream()
              .parallel()
              .forEach(
                  vdu ->
                      vdu.getVnfc_instance()
                          .stream()
                          .parallel()
                          .forEach(
                              vnfcInstance -> {
                                try {
                                  resourceManagement.release(vdu, vnfcInstance);
                                } catch (VimException
                                    | ExecutionException
                                    | PluginException
                                    | InterruptedException e) {
                                  e.printStackTrace();
                                }
                              }));
          if (nsrRepository.exists(virtualNetworkFunctionRecord.getParent_ns_id())) {
            NetworkServiceRecord nsr =
                nsrRepository.findFirstById(virtualNetworkFunctionRecord.getParent_ns_id());
            synchronized (NetworkServiceRecordManagement.class) {
              try {
                nsr.setVnfr(new HashSet<>());
                nsrRepository.delete(nsr);
                ApplicationEventNFVO event =
                    new ApplicationEventNFVO(
                        Action.RELEASE_RESOURCES_FINISH,
                        nsr,
                        virtualNetworkFunctionRecord.getProjectId());
                EventNFVO eventNFVO = new EventNFVO(this);
                eventNFVO.setEventNFVO(event);
                log.trace("Publishing event: " + event);
                publisher.dispatchEvent(eventNFVO);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
