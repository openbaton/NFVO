package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.common.catalogue.nfvo.Network;
import org.project.openbaton.common.catalogue.nfvo.Subnet;
import org.project.openbaton.common.catalogue.nfvo.VimInstance;
import org.project.openbaton.common.catalogue.util.IdGenerator;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
    @Qualifier("networkRepository")
    private GenericRepository<Network> networkRepository;

    @Override
    public Network add(VimInstance vimInstance, Network network) throws VimException{
        org.project.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
        vim = vimBroker.getVim(vimInstance.getType());
        //Define Network if values are null or empty
        if (network.getName() == null || network.getName().isEmpty())
            network.setName(IdGenerator.createUUID());
        if (network.getSubnets().size() == 0) {
            //Define Subnet
            Subnet subnet = new Subnet();
            subnet.setName(network.getName() + "_subnet");
            subnet.setCidr("192.169." + (int) Math.random() * 255 + ".0/24");
            //Define list of Subnets for Network
            Set<Subnet> subnets = new HashSet<Subnet>();
            subnets.add(subnet);
            network.setSubnets(subnets);
        }
        //Create Network on cloud environment
        network = vim.add(vimInstance, network);
        //Create Network in NetworkRepository
        networkRepository.create(network);
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
        networkRepository.remove(network);
    }

    @Override
    public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException{
        //Fetch Vim
        org.project.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement vim;
        vim = vimBroker.getVim(vimInstance.getType());
        //Update network on cloud environment
        return vim.update(vimInstance, updatingNetwork);
    }

    @Override
    public List<Network> query() {
        return networkRepository.findAll();
    }

    @Override
    public Network query(String id) {
        return networkRepository.find(id);
    }
}
