package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.core.utils.Utils;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
public class NetworkServiceRecordManagement implements org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("NSRRepository")
    private GenericRepository<NetworkServiceRecord> nsrRepository;

    @Override
    public NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) {
        NetworkServiceRecord networkServiceRecord = Utils.createNetworkServiceRecord(networkServiceDescriptor);
        log.trace("Deploying " + networkServiceRecord);
        nsrRepository.create(networkServiceRecord);
        log.debug("created NetworkServiceRecord with id " + networkServiceRecord.getId());
        return networkServiceRecord;
    }

    @Override
    public NetworkServiceRecord update(NetworkServiceRecord new_nsd, String old_id) {
        throw new NotImplementedException();
    }

    @Override
    public List<NetworkServiceRecord> query() {
        return nsrRepository.findAll();
    }

    @Override
    public NetworkServiceRecord query(String id) {
        return nsrRepository.find(id);
    }

    @Override
    public void delete(String id) {
        nsrRepository.remove(nsrRepository.find(id));
    }
}
