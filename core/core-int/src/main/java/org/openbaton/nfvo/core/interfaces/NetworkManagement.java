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

import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;

/** Created by mpa on 30/04/15. */
public interface NetworkManagement {

  /**
   * Add a network to a VIM instance.
   *
   * @param vimInstance vim to which the network is added
   * @param network network to add
   * @return added network
   * @throws VimException exception
   * @throws PluginException exception
   * @throws BadRequestException exception
   */
  BaseNetwork add(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimException, PluginException, BadRequestException;

  /**
   * Delete a network from a VIM instance.
   *
   * @param vimInstance vim from which to delete network
   * @param network network to delete
   * @throws VimException exception
   * @throws PluginException exception
   */
  void delete(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimException, PluginException;

  /**
   * Update an existing network on a VIM instance.
   *
   * @param vimInstance vim on which to update the network
   * @param new_network the new network
   * @return the updated network
   * @throws VimException exception
   * @throws PluginException exception
   */
  BaseNetwork update(BaseVimInstance vimInstance, Network new_network)
      throws VimException, PluginException;

  /**
   * Return all networks.
   *
   * @return the networks
   */
  Iterable<BaseNetwork> query();

  /**
   * Return a network specified by its ID.
   *
   * @param id ID of network
   * @return the network
   */
  BaseNetwork query(String id);
}
