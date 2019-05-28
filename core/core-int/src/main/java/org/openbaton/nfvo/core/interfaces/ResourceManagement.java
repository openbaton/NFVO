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

package org.openbaton.nfvo.core.interfaces;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.security.Key;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;

/** Created by mpa on 30/04/15. */
public interface ResourceManagement {

  /**
   * This operation allows requesting the instantiation and assignment of a virtualised resource to
   * the VNF, as indicated by the consumer functional block.
   *
   * @param virtualDeploymentUnit
   * @param virtualNetworkFunctionRecord
   * @param vimInstance
   * @param userdata
   * @param keys
   * @return
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws VimException
   * @throws PluginException
   */
  Future<List<String>> allocate(
      VirtualDeploymentUnit virtualDeploymentUnit,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      BaseVimInstance vimInstance,
      String userdata,
      Set<Key> keys)
      throws ExecutionException, InterruptedException, VimException, PluginException;

  /**
   * This operation allows querying a virtualised resource, i.e. retrieve information about an
   * instantiated virtualised resource.
   *
   * @param vimInstance
   * @return
   * @throws VimException
   * @throws PluginException
   */
  List<Server> query(BaseVimInstance vimInstance) throws VimException, PluginException;

  /**
   * This operation allows updating the configuration and/or parameterization of an instantiated
   * virtualised resource.
   *
   * @param vdu
   */
  void update(VirtualDeploymentUnit vdu);

  /**
   * This operation allows scaling a virtualised resource by adding or removing capacity, e.g.
   * adding vCPUs to a virtual machine.
   *
   * @param vdu
   */
  void scale(VirtualDeploymentUnit vdu);

  /**
   * This operation allows moving virtualised resources between locations. For instance, the
   * operation performs the migration of a computing resource from one host to another host; while
   * for a storage resource, it migrates the resource from one storage location to another.
   *
   * @param vdu
   */
  void migrate(VirtualDeploymentUnit vdu);

  /**
   * This operation allows executing specific commands on certain allocated virtualised resources.
   * Examples on compute resources can include (but not limited to): start, stop, pause, suspend,
   * capture snapshot, etc.
   *
   * @param vdu
   * @param operation
   * @return
   * @throws PluginException
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws VimException
   */
  Future<Void> operate(VirtualDeploymentUnit vdu, String operation)
      throws PluginException, ExecutionException, InterruptedException, VimException;

  /**
   * This operation allows de-allocating and terminating an instantiated virtualised resource. This
   * operation frees resources and returns them to the NFVI resource pool.
   *
   * @param vdu
   * @param vnfcInstance
   * @return
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws PluginException
   */
  Future<Void> release(VirtualDeploymentUnit vdu, VNFCInstance vnfcInstance)
      throws ExecutionException, InterruptedException, PluginException, VimException;

  /**
   * This operation allows requesting the reservation of a set of virtualised resources to a
   * consumer functional block without performing the steps of "Allocate Resource".
   *
   * @param vdu
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
   * @param vdu
   */
  void updateReservation(VirtualDeploymentUnit vdu);

  /**
   * This operation allows releasing an issued resources reservation, hence freeing the reserved
   * virtualised resources.
   *
   * @param vdu
   */
  void releaseReservation(VirtualDeploymentUnit vdu);

  /**
   * Allocate resources on a VIM for a VNFC instance.
   *
   * @param vdu
   * @param virtualNetworkFunctionRecord
   * @param componentToAdd
   * @param vimInstance
   * @param userdata
   * @return
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws PluginException
   * @throws VimException
   */
  Future<VNFCInstance> allocate(
      VirtualDeploymentUnit vdu,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFComponent componentToAdd,
      BaseVimInstance vimInstance,
      String userdata)
      throws InterruptedException, ExecutionException, PluginException, VimException;
}
