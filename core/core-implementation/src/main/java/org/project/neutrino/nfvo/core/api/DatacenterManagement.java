package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.nfvo.Datacenter;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope
public class DatacenterManagement implements org.project.neutrino.nfvo.core.interfaces.DatacenterManagement {
    @Autowired
    @Qualifier("datacenterRepository")
    private GenericRepository<Datacenter> datacenterRepository;

    @Override
    public Datacenter add(Datacenter datacenter) {
        return datacenterRepository.create(datacenter);
    }

    @Override
    public void delete(String id) {
        datacenterRepository.remove(datacenterRepository.find(id));
    }

    @Override
    public Datacenter update(Datacenter new_datacenter, String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Datacenter> query() {
        return datacenterRepository.findAll();
    }

    @Override
    public Datacenter query(String id) {
        return datacenterRepository.find(id);
    }
}
