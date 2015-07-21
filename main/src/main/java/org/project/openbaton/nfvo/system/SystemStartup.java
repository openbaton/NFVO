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

package org.project.openbaton.nfvo.system;

import org.project.openbaton.catalogue.nfvo.Configuration;
import org.project.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * Created by lto on 12/05/15.
 */
@Component
@Order(value = 1)
class SystemStartup implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    @Qualifier("configurationRepository")
    private GenericRepository<Configuration> configurationRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing NEUTRINO");

//        defaultMessageListenerContainer.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONNECTION);

        ClassPathResource classPathResource = new ClassPathResource("openbaton.properties");
        Properties properties = new Properties();
        properties.load(classPathResource.getInputStream());

        log.debug("Config Values are: " + properties.values());

        Configuration c = new Configuration();

        c.setName("system");
        c.setConfigurationParameters(new HashSet<ConfigurationParameter>());

//        configurationRepository.create(c);

        /**
         * Adding properties from file
         */
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            ConfigurationParameter cp = new ConfigurationParameter();
            cp.setKey((String) entry.getKey());
            cp.setValue((String) entry.getValue());
            c.getConfigurationParameters().add(cp);
        }

        /**
         * Adding system properties
         */

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(nets)){
            ConfigurationParameter cp = new ConfigurationParameter();
            log.trace("Display name: "+ netint.getDisplayName());
            log.trace("Name: "+ netint.getName());
            cp.setKey("ip-" + netint.getName().replaceAll("\\s",""));
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress.getHostAddress().contains(".")) {
                    log.trace("InetAddress: " + inetAddress.getHostAddress());
                    cp.setValue(inetAddress.getHostAddress());
                }
            }
            log.trace("");
            c.getConfigurationParameters().add(cp);
        }

        configurationRepository.create(c);

    }
}
