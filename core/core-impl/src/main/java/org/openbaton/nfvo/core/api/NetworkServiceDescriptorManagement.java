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

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongStatusException;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.PhysicalNetworkFunctionDescriptorRepository;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VNFDependencyRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

/** Created by lto on 11/05/15. */
@Service
@Scope
@ConfigurationProperties
public class NetworkServiceDescriptorManagement
    implements org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.vnfd.cascade.delete:false}")
  private boolean cascadeDelete;

  @Value("${nfvo.marketplace.ip:marketplace.openbaton.org}")
  private String marketIp;

  @Value("${nfvo.marketplace.port:8082}")
  private int marketPort;

  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VnfmEndpointRepository vnfmManagerEndpointRepository;
  @Autowired private VNFDependencyRepository vnfDependencyRepository;
  @Autowired private PhysicalNetworkFunctionDescriptorRepository pnfDescriptorRepository;
  @Autowired private NSDUtils nsdUtils;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  @Autowired private VirtualNetworkFunctionManagement virtualNetworkFunctionManagement;
  @Autowired private VNFPackageManagement vnfPackageManagement;
  @Autowired private Gson gson;

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
          CyclicDependenciesException, EntityInUseException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    networkServiceDescriptor.setProjectId(projectId);
    log.info("Starting onboarding process for NSD: " + networkServiceDescriptor.getName());

    nsdUtils.fetchExistingVnfd(networkServiceDescriptor);

    if (networkServiceDescriptor.getVld() != null) {
      for (VirtualLinkDescriptor vld : networkServiceDescriptor.getVld()) {
        if (vld.getName() == null || vld.getName().equals(""))
          throw new NotFoundException("A VLD in the NSD has no name specified!");
      }
    }

    log.info("Checking if Vnfm is running...");

    Iterable<VnfmManagerEndpoint> endpoints = vnfmManagerEndpointRepository.findAll();

    nsdUtils.checkEndpoint(networkServiceDescriptor, endpoints);

    log.trace("Creating " + networkServiceDescriptor);
    //    log.trace("Fetching Data");
    //    nsdUtils.fetchVimInstances(networkServiceDescriptor, projectId);
    //    log.trace("Fetched Data");

    for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
      vnfd.setCreatedAt(format.format(new Date()));
      vnfd.setUpdatedAt(format.format(new Date()));
      vnfd.setProjectId(projectId);
    }

    log.debug("Checking integrity of NetworkServiceDescriptor");
    nsdUtils.checkIntegrity(networkServiceDescriptor);

    log.trace("Persisting VNFDependencies");
    nsdUtils.fetchDependencies(networkServiceDescriptor);
    log.trace("Persisted VNFDependencies");

    networkServiceDescriptor.setProjectId(projectId);
    try {
      networkServiceDescriptor.setCreatedAt(format.format(new Date()));
      networkServiceDescriptor.setUpdatedAt(format.format(new Date()));
      networkServiceDescriptor = nsdRepository.save(networkServiceDescriptor);
    } catch (Exception e) {
      throw new org.openbaton.exceptions.EntityInUseException(
          "One of the VNF chosen is already in use by another NS");
    }
    log.info("Created NetworkServiceDescriptor with id " + networkServiceDescriptor.getId());
    return networkServiceDescriptor;
  }

  @Override
  public NetworkServiceDescriptor onboardFromMarketplace(String link, String projectId)
      throws BadFormatException, CyclicDependenciesException, NetworkServiceIntegrityException,
          NotFoundException, IOException, PluginException, VimException, IncompatibleVNFPackage,
          AlreadyExistingException, EntityInUseException, BadRequestException, InterruptedException,
          EntityUnreachableException {

    InputStream in = new BufferedInputStream(new URL(link).openStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] bytes = new byte[1024];
    int n = 0;
    while (-1 != (n = in.read(bytes))) {
      out.write(bytes, 0, n);
    }
    out.close();
    in.close();
    String json = out.toString();
    NetworkServiceDescriptor nsd = gson.fromJson(json, NetworkServiceDescriptor.class);

    List<String> market_ids = new ArrayList<>();
    for (VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()) {
      market_ids.add(vnfd.getId());
    }
    nsd.getVnfd().clear();
    List<String> vnfd_ids = getIds(market_ids, projectId);
    log.debug("Catalogue ids of VNFD are: " + vnfd_ids);
    for (String vnfd_id : vnfd_ids) {
      VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
      vnfd.setId(vnfd_id);
      nsd.getVnfd().add(vnfd);
    }
    return onboard(nsd, projectId);
  }

  private List<String> getIds(List<String> market_ids, String project_id)
      throws NotFoundException, IOException, PluginException, VimException, IncompatibleVNFPackage,
          AlreadyExistingException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException, EntityUnreachableException {
    List<String> not_found_ids = new ArrayList<>();
    not_found_ids.addAll(market_ids);
    List<String> vnfdIds = new ArrayList<>();
    for (String id : market_ids) {
      for (VirtualNetworkFunctionDescriptor vnfd : vnfdRepository.findByProjectId(project_id)) {
        String localId = vnfd.getVendor() + "/" + vnfd.getName() + "/" + vnfd.getVersion();
        String vnfdId = vnfd.getId();
        log.debug(localId);
        if (localId.toLowerCase().equals(id.toLowerCase())) {
          log.info("The vnfd " + localId + " was found onboarded on the same project.");
          vnfdIds.add(vnfdId);
          not_found_ids.remove(id);
        }
      }
    }
    log.debug("VNFDs found on the catalogue: " + vnfdIds);
    for (String id : not_found_ids) {
      String link = "http://" + marketIp + ":" + marketPort + "/api/v1/vnf-packages/" + id + "/tar";
      VirtualNetworkFunctionDescriptor vnfd =
          vnfPackageManagement.onboardFromMarket(link, project_id);
      log.info(
          "Onboarded from marketplace VNFD " + vnfd.getName() + " local id is: " + vnfd.getId());
      vnfdIds.add(vnfd.getId());
    }
    return vnfdIds;
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
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    networkServiceDescriptor.setUpdatedAt(format.format(new Date()));
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
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    networkServiceDescriptor.setUpdatedAt(format.format(new Date()));
    return networkServiceDescriptor.isEnabled();
  }

  /**
   * This operation allows updating a Network Service Descriptor (NSD), including any related VNFFGD
   * and VLD.This update might include creating/deleting new VNFFGDs and/or new VLDs.
   *
   * @param newNsd : the new values to be updated
   */
  @Override
  public NetworkServiceDescriptor update(NetworkServiceDescriptor newNsd, String projectId)
      throws NotFoundException, BadRequestException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    if (newNsd.getId() == null || newNsd.getId().isEmpty()) {
      throw new BadRequestException("No id found in the passed NSD");
    }
    NetworkServiceDescriptor updatingNsd = nsdRepository.findFirstById(newNsd.getId());
    if (updatingNsd == null) {
      throw new NotFoundException("Not found NSD with id " + newNsd.getId());
    } else if (!newNsd.getProjectId().equals(projectId)) {
      throw new UnauthorizedUserException(
          "NSD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    }
    updatingNsd.setUpdatedAt(format.format(new Date()));
    updatingNsd.setName(newNsd.getName());
    updatingNsd.setEnabled(newNsd.isEnabled());
    updatingNsd.setNfvo_version(newNsd.getNfvo_version());
    updatingNsd.setVendor(newNsd.getVendor());
    updatingNsd.setVersion(newNsd.getVersion());
    return nsdRepository.save(updatingNsd);
  }

  /**
   * This operation added a new VNFD to the NSD with {@code id}
   *
   * @param vnfd VirtualNetworkFunctionDescriptor to be persisted
   * @param id of NetworkServiceDescriptor
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
   * @param idNsd of NSD
   * @param idVnfd of VNFD
   */
  @Override
  public void deleteVnfDescriptor(String idNsd, String idVnfd, String projectId)
      throws EntityInUseException {
    log.debug("Is there a NSD referencing it? " + nsdRepository.exists(idNsd));
    if (nsdRepository.exists(idNsd)) {
      throw new EntityInUseException(
          "NSD with id: " + idNsd + " is still onboarded and referencing this VNFD");
    }
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
   * Returns the VirtualNetworkFunctionDescriptor selected by idVnfd into NSD with idNsd
   *
   * @param idNsd of NSD
   * @param idVnfd of VirtualNetworkFunctionDescriptor
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

  /** Removes the Security with idS from NSD with id */
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

  /** This operation is used to remove a disabled Network Service Descriptor. */
  @Override
  public void delete(String id, String projectId)
      throws WrongStatusException, EntityInUseException {
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
