package org.project.openbaton.nfvo.core.api;

import org.project.openbaton.common.catalogue.nfvo.Configuration;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
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
public class ConfigurationManagement implements org.project.openbaton.nfvo.core.interfaces.ConfigurationManagement {

    @Autowired
    @Qualifier("configurationRepository")
    private GenericRepository<Configuration> configurationRepository;

    @Override
    public Configuration add(Configuration datacenter) {
        return configurationRepository.create(datacenter);
    }

    @Override
    public void delete(String id) {
        configurationRepository.remove(configurationRepository.find(id));
    }

    @Override
    public Configuration update(Configuration configuration_new, String id) {
        Configuration old = configurationRepository.find(id);
        old.setName(configuration_new.getName());
        old.setParameters(configuration_new.getParameters());
        return old;

    }

    @Override
    public List<Configuration> query() {
        return configurationRepository.findAll();
    }

    @Override
    public Configuration query(String id) {
        return configurationRepository.find(id);
    }
}
