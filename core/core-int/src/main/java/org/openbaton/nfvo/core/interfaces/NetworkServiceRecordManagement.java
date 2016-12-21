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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongStatusException;

/** Created by mpa on 30/04/15. */
public interface NetworkServiceRecordManagement {

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   */
  NetworkServiceRecord onboard(
      String nsd_id, String projectId, List keys, Map vduVimInstances, Map configurations)
      throws VimException, NotFoundException, PluginException, MissingParameterException,
          BadRequestException;

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   */
  NetworkServiceRecord onboard(
      NetworkServiceDescriptor networkServiceDescriptor,
      String projectId,
      List keys,
      Map vduVimInstances,
      Map configurations)
      throws VimException, PluginException, NotFoundException, MissingParameterException,
          BadRequestException;

  /**
   * This operation allows updating a Network Service Descriptor (NSD), including any related VNFFGD
   * and VLD.This update might include creating/deleting new VNFFGDs and/or new VLDs.
   */
  NetworkServiceRecord update(NetworkServiceRecord new_nsd, String old_id, String projectId)
      throws NotFoundException;

  /**
   * This operation is used to query the information of the Network Service Descriptor (NSD),
   * including any related VNFFGD and VLD.
   */
  Iterable<NetworkServiceRecord> query();

  void executeAction(
      NFVMessage nfvMessage,
      String nsrId,
      String idVnf,
      String idVdu,
      String idVNFCI,
      String projectId)
      throws NotFoundException;

  NetworkServiceRecord query(String id, String projectId);

  /** This operation is used to remove a Network Service Record. */
  void delete(String id, String projectId) throws NotFoundException, WrongStatusException;

  /** This operation is used to resume a failed Network Service Record. */
  void resume(String id, String projectId) throws NotFoundException, WrongStatusException;

  void deleteVNFRecord(String idNsr, String idVnf, String projectId);

  /**
   * Returns the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
   *
   * @param idNsr of Nsr
   * @param idVnf of VirtualNetworkFunctionRecord
   * @return VirtualNetworkFunctionRecord selected
   */
  VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
      String idNsr, String idVnf, String projectId) throws NotFoundException;

  /**
   * Deletes the VNFDependency with idVnfr into NSR with idNsr
   *
   * @param idNsr of NSR
   * @param idVnfd of VNFDependency
   */
  void deleteVNFDependency(String idNsr, String idVnfd, String projectId);

  /**
   * This method will add a {@Link VNFCInstance} into a NetworkServiceRecord to a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord
   *
   * @param id of the NetworkServiceRecord
   * @param idVnf of the VirtualNetworkFunctionRecord
   * @param idVdu of the VirtualDeploymentUnit chosen
   * @return the new VNFCInstance
   */
  void addVNFCInstance(
      String id, String idVnf, String idVdu, VNFComponent component, String mode, String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException;

  /**
   * This method will add a {@Link VNFCInstance} into a NetworkServiceRecord to a specific
   * VirtualNetworkFunctionRecord. The VirtualDeploymentUnit is randomly chosen
   */
  void addVNFCInstance(String id, String idVnf, VNFComponent component, String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException;

  /**
   * This method will remove a {@Link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualNetworkFunctionRecord. VirtualDeploymentUnit will be randomly chosen.
   */
  void deleteVNFCInstance(String id, String idVnf, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException;

  /**
   * This method will remove a {@Link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord.
   */
  void deleteVNFCInstance(String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException;

  /**
   * This method will start a {@Link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord.
   */
  void startVNFCInstance(String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, WrongStatusException;

  /**
   * This method will stop a {@Link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord.
   */
  void stopVNFCInstance(String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, WrongStatusException;

  void switchToRedundantVNFCInstance(
      String id,
      String idVnf,
      String idVdu,
      String idVNFC,
      String standby,
      VNFCInstance failedVnfcInstance,
      String projectId)
      throws NotFoundException, WrongStatusException;

  void deleteVNFCInstance(String id, String idVnf, String idVdu, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException;

  List<NetworkServiceRecord> queryByProjectId(String projectId);
}
