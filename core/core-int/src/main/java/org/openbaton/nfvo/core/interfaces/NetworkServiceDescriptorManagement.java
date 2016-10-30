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

package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongStatusException;

import java.io.IOException;

import javax.persistence.NoResultException;

/**
 * Created by mpa on 30/04/15.
 */
public interface NetworkServiceDescriptorManagement {

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   */
  NetworkServiceDescriptor onboard(
      NetworkServiceDescriptor networkServiceDescriptor, String projectId)
      throws NotFoundException, BadFormatException, NetworkServiceIntegrityException,
          CyclicDependenciesException;

  NetworkServiceDescriptor onboardFromMarketplace(String link, String project_id)
      throws BadFormatException, CyclicDependenciesException, NetworkServiceIntegrityException,
          NotFoundException, IOException, PluginException, VimException, IncompatibleVNFPackage,
          AlreadyExistingException;

  /**
   * This operation allows disabling a Network Service Descriptor, so that it is not possible to
   * instantiate it any further.
   *
   * @param id
   */
  boolean disable(String id);

  /**
   * This operation allows enabling a Network Service Descriptor.
   *
   * @param id
   */
  boolean enable(String id);

  /**
   * This operation allows updating a Network Service Descriptor (NSD), including any related VNFFGD
   * and VLD.This update might include creating/deleting new VNFFGDs and/or new VLDs.
   *
   * @param new_nsd
   * @param projectId
   */
  NetworkServiceDescriptor update(NetworkServiceDescriptor new_nsd, String projectId);

  /**
   * This operation added a new VNFD to the NSD with {@code id}
   *
   * @param vnfd VirtualNetworkFunctionDescriptor to be persisted
   * @param id of NetworkServiceDescriptor
   * @param projectId
   * @return the persisted VirtualNetworkFunctionDescriptor
   */
  VirtualNetworkFunctionDescriptor addVnfd(
      VirtualNetworkFunctionDescriptor vnfd, String id, String projectId);

  /**
   * This operation is used to query the information of the Network Service Descriptor (NSD),
   * including any related VNFFGD and VLD.
   */
  Iterable<NetworkServiceDescriptor> query();

  NetworkServiceDescriptor query(String id, String projectId) throws NoResultException;

  /**
   * This operation is used to remove a disabled Network Service Descriptor.
   *
   * @param id
   */
  void delete(String id, String projectId) throws WrongStatusException, EntityInUseException;

  /**
   * Removes the VNFDescriptor into NSD
   *
   * @param nsd
   * @param idNsd of NSD
   * @param idVnfd of VNFD
   */
  void deleteVnfDescriptor(String nsd, String idNsd, String idVnfd) throws EntityInUseException;

  /**
   * Returns the VirtualNetworkFunctionDescriptor selected by idVnfd into NSD with idNsd
   *
   *
   * @param nsd
   * @param idNsd of NSD
   * @param idVnfd of VirtualNetworkFunctionDescriptor
   * @return VirtualNetworkFunctionDescriptor
   */
  VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor(
      String nsd, String idNsd, String idVnfd) throws NotFoundException;

  /**
   * Updates the VNFDescriptor into NSD with idNsd
   *
   * @param idNsd
   * @param idVfn
   * @param vnfDescriptor
   * @param projectId
   * @return
   */
  VirtualNetworkFunctionDescriptor updateVNF(
      String idNsd, String idVfn, VirtualNetworkFunctionDescriptor vnfDescriptor, String projectId);

  /**
   * Returns the VNFDependency selected by idVnfd into NSD with idNsd
   *
   * @return VNFDependency
   */
  VNFDependency getVnfDependency(String idNsd, String idVnfd, String projectId);

  /**
   * Removes the VNFDependency into NSD
   *
   * @param idNsd of NSD
   * @param idVnfd of VNFD
   * @param projectId
   */
  void deleteVNFDependency(String idNsd, String idVnfd, String projectId);

  /**
   * Save or Update the VNFDependency into NSD with idNsd
   *
   * @param idNsd
   * @param vnfDependency
   * @param projectId
   * @return VNFDependency
   */
  VNFDependency saveVNFDependency(String idNsd, VNFDependency vnfDependency, String projectId);

  /**
   * Deletes the PhysicalNetworkFunctionDescriptor from NSD
   *
   * @param idNsd of NSD
   * @param idPnf of PhysicalNetworkFunctionDescriptor
   * @param projectId
   */
  void deletePhysicalNetworkFunctionDescriptor(String idNsd, String idPnf, String projectId);

  /**
   * Returns the PhysicalNetworkFunctionDescriptor with idPnf into NSD with idNsd
   *
   * @param idNsd
   * @param idPnf
   * @param projectId
   * @return PhysicalNetworkFunctionDescriptor selected
   */
  PhysicalNetworkFunctionDescriptor getPhysicalNetworkFunctionDescriptor(
      String idNsd, String idPnf, String projectId) throws NotFoundException;

  /**
   * Adds or Updates the PhysicalNetworkFunctionDescriptor into NSD
   *
   * @param pDescriptor
   * @param id
   * @param projectId
   * @return PhysicalNetworkFunctionDescriptor
   */
  PhysicalNetworkFunctionDescriptor addPnfDescriptor(
      PhysicalNetworkFunctionDescriptor pDescriptor, String id, String projectId);

  /**
   * Adds or Updates the Security into NSD
   *
   * @param id
   * @param security
   * @param projectId
   * @return
   */
  Security addSecurity(String id, Security security, String projectId);

  /**
   * Removes the Secuty with idS from NSD with id
   *
   * @param id
   * @param idS
   * @param projectId
   */
  void deleteSecurty(String id, String idS, String projectId);

  Iterable<NetworkServiceDescriptor> queryByProjectId(String projectId);
}
