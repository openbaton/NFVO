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

package org.openbaton.nfvo.vim_interfaces.network_management;

import java.util.List;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.VimException;

public interface NetworkManagement {

  /**
   * This operation allows adding new Network to the network repository.
   *
   * @param vimInstance the {@link BaseVimInstance} to which add the network
   * @param network the {@link Network} to add
   * @return the {@link Network} added
   * @throws VimException in case of exception
   */
  BaseNetwork add(BaseVimInstance vimInstance, BaseNetwork network) throws VimException;

  /**
   * This operation allows deleting in the Networks from the network repository.
   *
   * @param vimInstance the {@link BaseVimInstance} in which delete the network
   * @param network the {@link Network} to delete
   * @throws VimException in case of exception
   */
  void delete(BaseVimInstance vimInstance, BaseNetwork network) throws VimException;

  /**
   * This operation allows updating the Network in the network repository.
   *
   * @param vimInstance the {@link BaseVimInstance} in which update the network
   * @param updatingNetwork the {@link Network} to update
   * @return the {@link Network} updated
   * @throws VimException in case of exception
   */
  BaseNetwork update(BaseVimInstance vimInstance, Network updatingNetwork) throws VimException;

  /**
   * This operation allows querying the information of the Networks in the network repository.
   *
   * @param vimInstance the {@link BaseVimInstance} to which query the networks
   * @return the list of all networks available
   * @throws VimException in case of exception
   */
  List<BaseNetwork> queryNetwork(BaseVimInstance vimInstance) throws VimException;

  /**
   * This operation allows querying the information of the Networks in the network repository.
   *
   * @param vimInstance the {@link BaseVimInstance} to which query the network
   * @param extId the external id of the Network to query
   * @return the {@link Network} queried
   * @throws VimException in case of exception
   */
  BaseNetwork query(BaseVimInstance vimInstance, String extId) throws VimException;
}
