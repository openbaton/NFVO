package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.nfvo.Configuration;
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
public class ConfigurationManagement implements org.project.neutrino.nfvo.core.interfaces.ConfigurationManagement {
    @Autowired
    @Qualifier("configurationRepository")
    private GenericRepository<Configuration> configurationGenericRepository;

    @Override
    public Configuration add(Configuration datacenter) {
        return configurationGenericRepository.create(datacenter);
    }

    @Override
    public void delete(String id) {
        configurationGenericRepository.remove(configurationGenericRepository.find(id));
    }

    @Override
    public Configuration update(Configuration new_datacenter, String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Configuration> query() {
        return configurationGenericRepository.findAll();
    }

    @Override
    public Configuration query(String id) {
        return configurationGenericRepository.find(id);
    }
}
