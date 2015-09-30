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

package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope
public class ConfigurationManagement implements org.openbaton.nfvo.core.interfaces.ConfigurationManagement {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Override
    public Configuration add(Configuration datacenter) {
        return configurationRepository.save(datacenter);
    }

    @Override
    public void delete(String id) {
        configurationRepository.delete(configurationRepository.findOne(id));
    }

    @Override
    public Configuration update(Configuration configuration_new, String id) {
        Configuration old = configurationRepository.findOne(id);
        old.setName(configuration_new.getName());
        old.setConfigurationParameters(configuration_new.getConfigurationParameters());
        return old;

    }

    @Override
    public Iterable<Configuration> query() {
        return configurationRepository.findAll();
    }

    @Override
    public Configuration query(String id) {
        return configurationRepository.findOne(id);
    }

    @Override
    public Configuration queryByName(String name) throws NotFoundException {
        Iterable<Configuration> configurations = query();
        for (Configuration configuration : configurations) {
            if (configuration.getName().equals(name)) {
                return configuration;
            }
        }
        throw new NotFoundException("Configuration with name " + name + " not found");
    }
}
