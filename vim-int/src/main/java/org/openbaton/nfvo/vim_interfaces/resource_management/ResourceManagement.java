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

package org.openbaton.nfvo.vim_interfaces.resource_management;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.VimException;

public interface ResourceManagement {

  /**
   * This operation allows requesting the instantiation and assignment of a virtualised resource to
   * the VNF, as indicated by the consumer functional block.
   *
   * @param vimInstance the {@link BaseVimInstance} on which allocate the resource
   * @param vdu the {@link VirtualDeploymentUnit}
   * @param virtualNetworkFunctionRecord the {@link VirtualNetworkFunctionRecord}
   * @param vnfComponent the {@link VNFComponent}
   * @param userdata the UserData to inject in the cloud init
   * @param floatingIps A map containing the network name as key and a string "random" or the static
   *     floating ip as string as value
   * @param keys the set of {@link Key} to add to the VM
   * @return the future containing the {@link VNFCInstance} deployed
   * @throws VimException in case of exception
   */
  Future<VNFCInstance> allocate(
      BaseVimInstance vimInstance,
      VirtualDeploymentUnit vdu,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent vnfComponent,
      String userdata,
      Map<String, String> floatingIps,
      Set<Key> keys)
      throws VimException;

  /**
   * This operation allows querying a virtualised resource, i.e. retrieve information about an
   * instantiated virtualised resource.
   *
   * @param vimInstance the {@link BaseVimInstance} on which query the list of {@link Server}s
   * @return the list of deplyed {@link Server}s
   * @throws VimException in case of exception
   */
  List<Server> queryResources(BaseVimInstance vimInstance) throws VimException;

  /**
   * This operation allows updating the configuration and/or parameterization of an instantiated
   * virtualised resource.
   *
   * @param vdu the {@link VirtualDeploymentUnit} to update
   */
  void update(VirtualDeploymentUnit vdu);

  /**
   * This operation allows scaling a virtualised resource by adding or removing capacity, e.g.
   * adding vCPUs to a virtual machine.
   *
   * @param vdu the {@link VirtualDeploymentUnit} to scale
   */
  void scale(VirtualDeploymentUnit vdu);

  /**
   * This operation allows moving virtualised resources between locations. For instance, the
   * operation performs the migration of a computing resource from one host to another host; while
   * for a storage resource, it migrates the resource from one storage location to another.
   *
   * @param vdu the {@link VirtualDeploymentUnit} to migrate
   */
  void migrate(VirtualDeploymentUnit vdu);

  /**
   * This operation allows executing specific commands on certain allocated virtualised resources.
   * Examples on compute resources can include (but not limited to): start, stop, pause, suspend,
   * capture snapshot, etc.
   *
   * @param vimInstance the {@link BaseVimInstance} on which to operate
   * @param vdu the {@link VirtualDeploymentUnit} on which to operate
   * @param operation the operation to perform
   * @return Future of {@link Void}
   * @throws VimException in case of exception
   */
  Future<Void> operate(BaseVimInstance vimInstance, VirtualDeploymentUnit vdu, String operation)
      throws VimException;

  /**
   * This operation allows de-allocating and terminating an instantiated virtualised resource. This
   * operation frees resources and returns them to the NFVI resource pool.
   *
   * @param vnfcInstance the {@link VNFCInstance} to deallocate
   * @param vimInstance the {@link BaseVimInstance} on which deallocate the resource
   * @return Future of {@link Void}
   * @throws VimException in case of exception
   */
  Future<Void> release(VNFCInstance vnfcInstance, BaseVimInstance vimInstance) throws VimException;

  /**
   * This operation allows requesting the reservation of a set of virtualised resources to a
   * consumer functional block without performing the steps of "Allocate Resource".
   *
   * @param vdu the {@link VirtualDeploymentUnit} to reserve
   */
  void createReservation(VirtualDeploymentUnit vdu);

  /**
   * This operation allows querying an issued resources reservation, e.g. to discover the
   * virtualised resources included in a specific reserved resources pool, or the amount of free
   * resources in such a pool.
   */
  void queryReservation();

  /**
   * This operation allows updating an issued resources reservation to increase or decrease the
   * amount of virtualised resources in the reserved resources pool.
   *
   * @param vdu the {@link VirtualDeploymentUnit} to update the reservation
   */
  void updateReservation(VirtualDeploymentUnit vdu);

  /**
   * This operation allows releasing an issued resources reservation, hence freeing the reserved
   * virtualised resources.
   *
   * @param vdu the the {@link VirtualDeploymentUnit} to which the reservation to release was made
   */
  void releaseReservation(VirtualDeploymentUnit vdu);

  /**
   * This operations return the maximal Quotas allowed to allocate.
   *
   * @param vimInstance the {@link BaseVimInstance} on which requesting the quota
   * @return quota the {@link Quota} for that specific {@link BaseVimInstance}
   * @throws VimException in case of exception
   */
  Quota getQuota(BaseVimInstance vimInstance) throws VimException;

  BaseVimInstance refresh(BaseVimInstance vimInstance) throws VimException;
}
