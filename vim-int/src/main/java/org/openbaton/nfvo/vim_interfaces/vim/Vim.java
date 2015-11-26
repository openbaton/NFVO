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

package org.openbaton.nfvo.vim_interfaces.vim;

import org.openbaton.exceptions.PluginException;
import org.openbaton.nfvo.vim_interfaces.flavor_management.DeploymentFlavorManagement;
import org.openbaton.nfvo.vim_interfaces.image_management.ImageManagement;
import org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement;
import org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement;
import org.openbaton.plugin.utils.PluginBroker;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.openbaton.vim.drivers.interfaces.ClientInterfaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by mpa on 12.06.15.
 */
public abstract class Vim implements ImageManagement, ResourceManagement, NetworkManagement, DeploymentFlavorManagement {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected VimDriverCaller client;
    protected PluginBroker<ClientInterfaces> pluginBroker;
    @Autowired
    private ConfigurableApplicationContext context;

    public Vim(String type, String brokerIp, int port) throws PluginException {
        if (client == null && context != null) {
            pluginBroker = new PluginBroker<>();
            client = (VimDriverCaller) context.getBean("vimDriverCaller", brokerIp, port, type);
            if (client == null) {
                throw new PluginException("No bean of VimDriverCaller found");
            }
        }
    }

    public Vim(String type) throws PluginException {
        if (client == null && context != null) {
            pluginBroker = new PluginBroker<>();
            client = (VimDriverCaller) context.getBean("vimDriverCaller", type);
            if (client == null) {
                throw new PluginException("No bean of VimDriverCaller found");
            }
        }
    }

    public Vim(String type, String name) throws PluginException {
        if (client == null && context != null) {
            pluginBroker = new PluginBroker<>();
            client = (VimDriverCaller) context.getBean("vimDriverCaller", name, type);
            if (client == null) {
                throw new PluginException("No bean of VimDriverCaller found");
            }
        }
    }

    public Vim(String type, String username, String password, String brokerIp) throws PluginException {
        if (client == null && context != null) {
            pluginBroker = new PluginBroker<>();
            client = (VimDriverCaller) context.getBean("vimDriverCaller", brokerIp, username, password, type);
            if (client == null) {
                throw new PluginException("No bean of VimDriverCaller found");
            }
        }
    }

    public VimDriverCaller getClient() {
        return client;
    }

    public void setClient(VimDriverCaller client) {
        this.client = client;
    }
}
