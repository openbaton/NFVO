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

package org.openbaton.nfvo.core.core;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.net.util.SubnetUtils;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.networks.Subnet;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.util.IdGenerator;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.openbaton.nfvo.repositories.NetworkRepository;
import org.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope
public class NetworkManagement implements org.openbaton.nfvo.core.interfaces.NetworkManagement {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VimBroker vimBroker;

  @Autowired private NetworkRepository networkRepository;
  @Autowired private VimManagement vimInstanceRepository;

  @Override
  public synchronized BaseNetwork add(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimException, PluginException, BadRequestException {
    log.info("Creating network " + network.getName() + " on vim " + vimInstance.getName());
    org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
    vim = vimBroker.getVim(vimInstance.getType());
    //Define Network if values are null or empty
    if (network.getName() == null || network.getName().isEmpty())
      network.setName(IdGenerator.createUUID());
    if (network instanceof Network) {
      Network osNetwork = (Network) network;
      if (osNetwork.getSubnets().isEmpty()) {
        //Define Subnet
        Subnet subnet = new Subnet();
        subnet.setName(network.getName() + "_subnet");
        subnet.setCidr("192.168." + (int) (Math.random() * 255) + ".0/24");
        //Define list of Subnets for Network
        Set<Subnet> subnets = new HashSet<>();
        subnets.add(subnet);
        osNetwork.setSubnets(subnets);
      } else {
        for (Subnet subnet : osNetwork.getSubnets()) {
          if (subnet.getName() == null || subnet.getName().equals(""))
            subnet.setName(network.getName() + "_subnet");
          if (subnet.getCidr() == null || subnet.getCidr().equals(""))
            subnet.setCidr("192.168." + (int) (Math.random() * 255) + ".0/24");
          try {
            new SubnetUtils(subnet.getCidr());
          } catch (IllegalArgumentException e) {
            log.error(String.format("Cidr : %s is not valid", subnet.getCidr()));
            throw new BadRequestException(
                String.format("Cidr : %s is not valid", subnet.getCidr()));
          }
        }
      }
    }
    //Create Network on cloud environment
    network = vim.add(vimInstance, network);
    //Create Network in NetworkRepository
    network = networkRepository.save(network);
    //Add network to VimInstance
    vimInstance.addNetwork(network);
    log.info("Created Network " + network.getName());
    log.debug("Network details: " + network);
    return network;
  }

  @Override
  public void delete(BaseVimInstance vimInstance, BaseNetwork network)
      throws VimException, PluginException {
    //Fetch Vim
    org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
    vim = vimBroker.getVim(vimInstance.getType());
    //Delete network from cloud environment
    vim.delete(vimInstance, network);
    //Delete network from NetworkRepository
    networkRepository.delete(network);
  }

  @Override
  public BaseNetwork update(BaseVimInstance vimInstance, Network updatingNetwork)
      throws VimException, PluginException {
    //Fetch Vim
    org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
    vim = vimBroker.getVim(vimInstance.getType());
    //Update network on cloud environment
    return vim.update(vimInstance, updatingNetwork);
  }

  @Override
  public Iterable<BaseNetwork> query() {
    return networkRepository.findAll();
  }

  @Override
  public BaseNetwork query(String id) {
    return networkRepository.findOne(id);
  }
}
