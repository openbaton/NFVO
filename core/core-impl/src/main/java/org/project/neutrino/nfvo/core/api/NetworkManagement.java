package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 20/05/15.
 */
@Service
@Scope
public class NetworkManagement implements org.project.neutrino.nfvo.core.interfaces.NetworkManagement {

    @Autowired
    @Qualifier("networkRepository")
    private GenericRepository<Network> networkRepository;

    @Override
    public Network add(Network network) {
        return networkRepository.create(network);
    }

    @Override
    public void delete(String id) {
        networkRepository.remove(networkRepository.find(id));
    }

    @Override
    public Network update(Network network_new, String id) {
        Network old = networkRepository.find(id);
        old.setExternal(network_new.getExternal());
        old.setName(network_new.getName());
        old.setNetworkType(network_new.getNetworkType());
        old.setPhysicalNetworkName(network_new.getPhysicalNetworkName());
        old.setShared(network_new.getShared());
        old.setExtId(network_new.getExtId());
        old.setSubnets(network_new.getSubnets());
        return old;
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
