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

package org.openbaton.nfvo.system;

import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.nfvo.repositories.ConfigurationRepository;
import org.openbaton.plugin.utils.PluginStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties
class SystemStartup implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Value("${nfvo.plugin.active.consumers:}")
    private String numConsumers;

    @Value("${spring.rabbitmq.username:}")
    private String username;
    @Value("${spring.rabbitmq.password:}")
    private String password;
    @Value("${nfvo.rabbit.management.port:}")
    private String  managementPort;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing OpenBaton");

        InputStream is = new FileInputStream("/etc/openbaton/openbaton.properties");
        Properties properties = new Properties();
        properties.load(is);

        log.debug("Config Values are: " + properties.values());

        Configuration c = new Configuration();

        c.setName("system");
        c.setConfigurationParameters(new HashSet<ConfigurationParameter>());

        /**
         * Adding properties from file
         */
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            ConfigurationParameter cp = new ConfigurationParameter();
            cp.setConfKey((String) entry.getKey());
            cp.setValue((String) entry.getValue());
            c.getConfigurationParameters().add(cp);
        }

        /**
         * Adding system properties
         */

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            ConfigurationParameter cp = new ConfigurationParameter();
            log.trace("Display name: " + netint.getDisplayName());
            log.trace("Name: " + netint.getName());
            cp.setConfKey("ip-" + netint.getName().replaceAll("\\s", ""));
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


        configurationRepository.save(c);

        if (Boolean.parseBoolean(properties.getProperty("install-plugin","true"))) {
            startPlugins(properties.getProperty("plugin-installation-dir", "./plugins"));
        }
    }

    private void startPlugins(String folderPath) throws IOException {
        if (numConsumers == null || numConsumers.equals(""))
            numConsumers = "" + 5;
        if (username == null || username.equals(""))
            username = "admin";
        if (password == null || password.equals(""))
            password = "openbaton";
        if (managementPort == null || managementPort.equals(""))
            managementPort = "15672";
        PluginStartup.startPluginRecursive(folderPath, false, "localhost", "5672", Integer.parseInt(numConsumers), username, password, managementPort);
    }

}
