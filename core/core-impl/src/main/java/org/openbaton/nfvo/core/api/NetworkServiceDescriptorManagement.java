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

import org.apache.commons.validator.routines.UrlValidator;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.WrongStatusException;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
@ConfigurationProperties
public class NetworkServiceDescriptorManagement
    implements org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.vnfd.cascade.delete:false}")
  private boolean cascadeDelete;

  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VnfmEndpointRepository vnfmManagerEndpointRepository;
  @Autowired private VNFDependencyRepository vnfDependencyRepository;
  @Autowired private PhysicalNetworkFunctionDescriptorRepository pnfDescriptorRepository;
  @Autowired private NSDUtils nsdUtils;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VirtualNetworkFunctionManagement virtualNetworkFunctionManagement;

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public void setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
  }

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   */
  @Override
  public NetworkServiceDescriptor onboard(
      NetworkServiceDescriptor networkServiceDescriptor, String projectId)
      throws NotFoundException, BadFormatException, NetworkServiceIntegrityException,
          CyclicDependenciesException {

    log.info("Staring onboarding process for NSD: " + networkServiceDescriptor.getName());
    UrlValidator urlValidator = new UrlValidator();

    nsdUtils.fetchExistingVnfd(networkServiceDescriptor);

    for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
      vnfd.setProjectId(projectId);
      for (VirtualDeploymentUnit virtualDeploymentUnit : vnfd.getVdu()) {
        virtualDeploymentUnit.setProjectId(projectId);
      }
      if (vnfd.getLifecycle_event() != null)
        for (LifecycleEvent event : vnfd.getLifecycle_event()) {
          if (event == null) {
            throw new NotFoundException("LifecycleEvent is null");
          } else if (event.getEvent() == null) {
            throw new NotFoundException("Event in one LifecycleEvent does not exist");
          }
        }
      if (vnfd.getEndpoint() == null) vnfd.setEndpoint(vnfd.getType());
      if (vnfd.getVnfPackageLocation() != null) {
        if (urlValidator.isValid(vnfd.getVnfPackageLocation())) { // this is a script link
          VNFPackage vnfPackage = new VNFPackage();
          vnfPackage.setScriptsLink(vnfd.getVnfPackageLocation());
          vnfPackage.setName(vnfd.getName());
          vnfPackage.setProjectId(projectId);
          vnfPackage = vnfPackageRepository.save(vnfPackage);
          vnfd.setVnfPackageLocation(vnfPackage.getId());
        } else { // this is an id pointing to a package already existing
          // nothing to do here i think...
        }
      } else log.warn("vnfPackageLocation is null. Are you sure?");
    }

    log.info("Checking if Vnfm is running...");

    Iterable<VnfmManagerEndpoint> endpoints = vnfmManagerEndpointRepository.findAll();

    nsdUtils.checkEndpoint(networkServiceDescriptor, endpoints);

    log.trace("Creating " + networkServiceDescriptor);
    log.trace("Fetching Data");
    nsdUtils.fetchVimInstances(networkServiceDescriptor, projectId);
    log.trace("Fetched Data");

    log.debug("Checking integrity of NetworkServiceDescriptor");
    nsdUtils.checkIntegrity(networkServiceDescriptor);

    log.trace("Persisting VNFDependencies");
    nsdUtils.fetchDependencies(networkServiceDescriptor);
    log.trace("Persisted VNFDependencies");

    networkServiceDescriptor.setProjectId(projectId);
    networkServiceDescriptor = nsdRepository.save(networkServiceDescriptor);
    log.info("Created NetworkServiceDescriptor with id " + networkServiceDescriptor.getId());
    return networkServiceDescriptor;
  }

  /**
   * This operation allows disabling a Network Service Descriptor, so that it is not possible to
   * instantiate it any further.
   *
   * @param id: the id of the {@Link NetworkServiceDescriptor} to disable
   */
  @Override
  public boolean disable(String id) throws NoResultException {
    log.debug("disabling NetworkServiceDescriptor with id " + id);
    NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(id);
    networkServiceDescriptor.setEnabled(false);
    return networkServiceDescriptor.isEnabled();
  }

  /**
   * This operation allows enabling a Network Service Descriptor.
   *
   * @param id: the id of the {@Link NetworkServiceDescriptor} to enable
   */
  @Override
  public boolean enable(String id) throws NoResultException {
    log.debug("enabling NetworkServiceDescriptor with id " + id);
    NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(id);
    networkServiceDescriptor.setEnabled(true);
    return networkServiceDescriptor.isEnabled();
  }

  /**
   * This operation allows updating a Network Service Descriptor (NSD), including any related VNFFGD
   * and VLD.This update might include creating/deleting new VNFFGDs and/or new VLDs.
   *
   * @param newNsd : the new values to be updated
   * @param projectId
   */
  @Override
  public NetworkServiceDescriptor update(NetworkServiceDescriptor newNsd, String projectId) {
    if (nsdRepository.findFirstById(newNsd.getId()).getProjectId().equals(projectId))
      return nsdRepository.save(newNsd);
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * This operation added a new VNFD to the NSD with {@code id}
   *
   * @param vnfd VirtualNetworkFunctionDescriptor to be persisted
   * @param id of NetworkServiceDescriptor
   * @param projectId
   * @return the persisted VirtualNetworkFunctionDescriptor
   */
  public VirtualNetworkFunctionDescriptor addVnfd(
      VirtualNetworkFunctionDescriptor vnfd, String id, String projectId) {
    if (vnfdRepository.findFirstById(vnfd.getId()).getProjectId().equals(projectId))
      return nsdRepository.addVnfd(vnfd, id);
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Removes the VNFDescriptor with idVnfd from NSD with idNsd
   *
   * @param nsd
   * @param idNsd of NSD
   * @param idVnfd of VNFD
   */
  @Override
  public void deleteVnfDescriptor(String idNsd, String idVnfd, String projectId) {
    log.info("Removing VnfDescriptor with id: " + idVnfd + " from NSD with id: " + idNsd);
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfdRepository.findFirstById(idVnfd);
    if (!virtualNetworkFunctionDescriptor.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    nsdRepository.deleteVnfd(idNsd, idVnfd);
    vnfPackageRepository.delete(virtualNetworkFunctionDescriptor.getVnfPackageLocation());
  }

  /**
   *
   * Returns the VirtualNetworkFunctionDescriptor selected by idVnfd into NSD with idNsd
   *
   * @param idNsd of NSD
   * @param idVnfd of VirtualNetworkFunctionDescriptor
   * @param projectId
   * @return
   * @throws NotFoundException
   */
  @Override
  public VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor(
      String idNsd, String idVnfd, String projectId) throws NotFoundException {
    nsdRepository.exists(idNsd);
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfdRepository.findFirstById(idVnfd);

    if (virtualNetworkFunctionDescriptor == null)
      throw new NotFoundException(
          "VirtualNetworkFunctionDescriptor with id " + idVnfd + " doesn't exist");

    if (!virtualNetworkFunctionDescriptor.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");

    return virtualNetworkFunctionDescriptor;
  }

  /**
   * Updates the VNFDescriptor into NSD with idNsd
   *
   * @param idNsd
   * @param idVfn
   * @param vnfDescriptor
   * @param projectId
   * @return VirtualNetworkFunctionDescriptor
   */
  @Override
  public VirtualNetworkFunctionDescriptor updateVNF(
      String idNsd,
      String idVfn,
      VirtualNetworkFunctionDescriptor vnfDescriptor,
      String projectId) {
    nsdRepository.exists(idNsd);
    if (!vnfdRepository.findFirstById(vnfDescriptor.getId()).getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    nsdRepository.addVnfd(vnfDescriptor, idNsd);
    return vnfDescriptor;
  }

  /**
   * Returns the VNFDependency selected by idVnfd into NSD with idNsd
   *
   * @param idNsd
   * @param idVnfd
   * @param projectId
   * @return VNFDependency
   */
  @Override
  public VNFDependency getVnfDependency(String idNsd, String idVnfd, String projectId) {
    if (nsdRepository.findFirstById(idNsd).getProjectId().equals(projectId))
      return vnfDependencyRepository.findOne(idVnfd);
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Removes the VNFDependency into NSD
   *
   * @param idNsd of NSD
   * @param idVnfd of VNFD
   * @param projectId
   */
  @Override
  public void deleteVNFDependency(String idNsd, String idVnfd, String projectId) {
    log.debug("Removing VNFDependency with id: " + idVnfd + " from NSD with id: " + idNsd);
    if (nsdRepository.findFirstById(idNsd).getProjectId().equals(projectId)) {
      nsdRepository.deleteVNFDependency(idNsd, idVnfd);
      return;
    }
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Save or Update the VNFDependency into NSD with idNsd
   *
   * @param idNsd
   * @param vnfDependency
   * @param projectId
   * @return VNFDependency
   */
  @Override
  public VNFDependency saveVNFDependency(
      String idNsd, VNFDependency vnfDependency, String projectId) {
    if (nsdRepository.findFirstById(idNsd).getProjectId().equals(projectId)) {
      nsdRepository.addVnfDependency(vnfDependency, idNsd);
      return vnfDependency;
    }
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Deletes the PhysicalNetworkFunctionDescriptor from NSD
   *
   * @param idNsd of NSD
   * @param idPnf of PhysicalNetworkFunctionDescriptor
   * @param projectId
   */
  @Override
  public void deletePhysicalNetworkFunctionDescriptor(
      String idNsd, String idPnf, String projectId) {
    if (nsdRepository.findFirstById(idNsd).getProjectId().equals(projectId))
      nsdRepository.deletePhysicalNetworkFunctionDescriptor(idNsd, idPnf);
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Returns the PhysicalNetworkFunctionDescriptor with idPnf into NSD with idNsd
   *
   * @param idNsd
   * @param idPnf
   * @param projectId
   * @return PhysicalNetworkFunctionDescriptor selected
   */
  @Override
  public PhysicalNetworkFunctionDescriptor getPhysicalNetworkFunctionDescriptor(
      String idNsd, String idPnf, String projectId) throws NotFoundException {
    if (nsdRepository.findFirstById(idNsd).getProjectId().equals(projectId)) {
      PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor =
          pnfDescriptorRepository.findOne(idPnf);
      if (physicalNetworkFunctionDescriptor == null)
        throw new NotFoundException(
            "PhysicalNetworkFunctionDescriptor with id " + idPnf + " doesn't exist");
      return physicalNetworkFunctionDescriptor;
    }
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Add or Update the PhysicalNetworkFunctionDescriptor into NSD
   *
   * @param pDescriptor
   * @param idNsd
   * @param projectId
   * @return PhysicalNetworkFunctionDescriptor
   */
  @Override
  public PhysicalNetworkFunctionDescriptor addPnfDescriptor(
      PhysicalNetworkFunctionDescriptor pDescriptor, String idNsd, String projectId) {
    if (nsdRepository.findFirstById(idNsd).getProjectId().equals(projectId)) {
      return nsdRepository.addPnfDescriptor(pDescriptor, idNsd);
    }
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Adds or Updates the Security into NSD
   *
   * @param id
   * @param security
   * @param projectId
   * @return Security
   */
  @Override
  public Security addSecurity(String id, Security security, String projectId) {
    if (nsdRepository.findFirstById(id).getProjectId().equals(projectId)) {
      return nsdRepository.addSecurity(id, security);
    }
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * Removes the Secuty with idS from NSD with id
   *
   * @param idNsd
   * @param idS
   * @param projectId
   */
  @Override
  public void deleteSecurty(String idNsd, String idS, String projectId) {
    if (nsdRepository.findFirstById(idNsd).getProjectId().equals(projectId)) {
      nsdRepository.deleteSecurity(idNsd, idS);
    }
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  @Override
  public Iterable<NetworkServiceDescriptor> queryByProjectId(String projectId) {
    return nsdRepository.findByProjectId(projectId);
  }

  /**
   * This operation is used to query the information of the Network Service Descriptor (NSD),
   * including any related VNFFGD and VLD.
   */
  @Override
  public Iterable<NetworkServiceDescriptor> query() {
    return nsdRepository.findAll();
  }

  /**
   * This operation is used to query the information of the Network Service Descriptor (NSD),
   * including any related VNFFGD and VLD.
   */
  @Override
  public NetworkServiceDescriptor query(String id, String projectId) throws NoResultException {
    NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(id);
    if (networkServiceDescriptor.getProjectId().equals(projectId)) return networkServiceDescriptor;
    throw new UnauthorizedUserException(
        "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  /**
   * This operation is used to remove a disabled Network Service Descriptor.
   *
   * @param id
   */
  @Override
  public void delete(String id, String projectId) throws WrongStatusException {
    log.info("Removing NetworkServiceDescriptor with id " + id);
    NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(id);
    if (!networkServiceDescriptor.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");

    for (NetworkServiceRecord nsr : nsrRepository.findAll()) {
      if (nsr.getDescriptor_reference().equals(id)) {
        if (nsr.getStatus().ordinal() != Status.ACTIVE.ordinal()) {
          throw new WrongStatusException(
              "The NetworkServiceRecord "
                  + nsr.getName()
                  + " created from the NetworkServiceDescriptor "
                  + networkServiceDescriptor.getName()
                  + " is not yet in ACTIVE");
        }
      }
    }

    nsdRepository.delete(networkServiceDescriptor);
    if (cascadeDelete) {
      for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
          networkServiceDescriptor.getVnfd()) {
        virtualNetworkFunctionManagement.delete(
            virtualNetworkFunctionDescriptor.getId(), projectId);
      }
    }
  }
}
