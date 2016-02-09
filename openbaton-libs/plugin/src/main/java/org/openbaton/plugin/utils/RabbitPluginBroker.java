/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.plugin.utils;

import org.openbaton.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 26/11/15.
 */
@Service
public class RabbitPluginBroker {

    @Autowired
    private ConfigurableApplicationContext context;

    static Logger log = LoggerFactory.getLogger(RabbitPluginBroker.class);

    public Object getVimDriverCaller(String type) {
        return context.getBean("vimDriverCaller", type);
    }

    public Object getVimDriverCaller(String name, String type) {
        return context.getBean("vimDriverCaller", name, type);
    }

    public Object getVimDriverCaller(String name, String type, String managementPort) {
        return context.getBean("vimDriverCaller", name, type, managementPort);
    }

    public Object getVimDriverCaller(String brokerIp, String username, String password, int port, String type, String managementPort) {
        return context.getBean("vimDriverCaller", brokerIp, username, password, port, type, managementPort);
    }

    public Object getVimDriverCaller(String brokerIp, String username, String password, int port, String type, String name, String managementPort) {
        return context.getBean("vimDriverCaller", brokerIp, username, password, port, type, name, managementPort);
    }

    public Object getVimDriverCaller(String brokerIp, String username, String password, String type, String managementPort) {
        return context.getBean("vimDriverCaller", brokerIp, username, password, type, managementPort);
    }

    /*
    Monitoring plugin
     */

    public Object getMonitoringPluginCaller(String type) {
        return context.getBean("monitoringPluginCaller", type);
    }

    public Object getMonitoringPluginCaller(String name, String type) {
        return context.getBean("monitoringPluginCaller", name, type);
    }

    public Object getMonitoringPluginCaller(String name, String type, String managementPort) {
        return context.getBean("monitoringPluginCaller", name, type, managementPort);
    }

    public Object getMonitoringPluginCaller(String brokerIp, String username, String password, int port, String type, String managementPort) {
        return context.getBean("monitoringPluginCaller", brokerIp, username, password, port, type, managementPort);
    }

    public Object getMonitoringPluginCaller(String brokerIp, String username, String password, int port, String type, String name, String managementPort) {
        return context.getBean("monitoringPluginCaller", brokerIp, username, password, port, type, name, managementPort);
    }

    public Object getMonitoringPluginCaller(String brokerIp, String username, String password, String type, String managementPort) {
        return context.getBean("monitoringPluginCaller", brokerIp, username, password, type, managementPort);
    }

}
