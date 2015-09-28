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

package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.nfvo.Network;
import org.project.openbaton.catalogue.nfvo.Subnet;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.catalogue.util.IdGenerator;
import org.project.openbaton.exceptions.VimException;
import org.project.openbaton.nfvo.repositories.NetworkRepository;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mpa on 24.06.15.
 */
@Service
@Scope
public class NetworkManagement implements org.project.openbaton.nfvo.core.interfaces.NetworkManagement {
    @Autowired
    private VimBroker vimBroker;

    @Autowired
    private NetworkRepository networkRepository;

    @Override
    public Network add(VimInstance vimInstance, Network network) throws VimException {
        org.project.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
        vim = vimBroker.getVim(vimInstance.getType());
        //Define Network if values are null or empty
        if (network.getName() == null || network.getName().isEmpty())
            network.setName(IdGenerator.createUUID());
        if (network.getSubnets().size() == 0) {
            //Define Subnet
            Subnet subnet = new Subnet();
            subnet.setName(network.getName() + "_subnet");
            subnet.setCidr("192.168." + (int)(Math.random() * 255) + ".0/24");
            //Define list of Subnets for Network
            Set<Subnet> subnets = new HashSet<Subnet>();
            subnets.add(subnet);
            network.setSubnets(subnets);
        }
        //Create Network on cloud environment
        network = vim.add(vimInstance, network);
        //Create Network in NetworkRepository
        networkRepository.save(network);
        //Add network to VimInstance
        vimInstance.getNetworks().add(network);
        return network;
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException {
        //Fetch Vim
        org.project.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
        vim = vimBroker.getVim(vimInstance.getType());
        //Delete network from cloud environment
        vim.delete(vimInstance, network);
        //Delete network from NetworkRepository
        networkRepository.delete(network);
    }

    @Override
    public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
        //Fetch Vim
        org.project.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
        vim = vimBroker.getVim(vimInstance.getType());
        //Update network on cloud environment
        return vim.update(vimInstance, updatingNetwork);
    }

    @Override
    public Iterable<Network> query() {
        return networkRepository.findAll();
    }

    @Override
    public Network query(String id) {
        return networkRepository.findOne(id);
    }
}
