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

import org.openbaton.nfvo.vim_interfaces.flavor_management.DeploymentFlavorManagement;
import org.openbaton.vim.drivers.interfaces.ClientInterfaces;
import org.openbaton.plugin.utils.PluginBroker;
import org.openbaton.nfvo.vim_interfaces.image_management.ImageManagement;
import org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement;
import org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by mpa on 12.06.15.
 */
public abstract class Vim implements ImageManagement, ResourceManagement, NetworkManagement, DeploymentFlavorManagement {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected ClientInterfaces client;

    protected PluginBroker<ClientInterfaces> pluginBroker;

    public Vim(String type, int port) {
        pluginBroker = new PluginBroker<>();
        try {
            client = pluginBroker.getPlugin("vim-drivers",type, port);
            log.trace("Class is: " + client.getClass().getName());
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        } catch (NotBoundException e) {
            log.error("No plugin found for type: " + type);
        }
    }

    public Vim(String type) {
        pluginBroker = new PluginBroker<>();
        try {
            client = pluginBroker.getPlugin("vim-drivers",type);
            log.trace("Class is: " + client.getClass().getName());
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        } catch (NotBoundException e) {
            log.error("No plugin found for type: " + type);
        }
    }

    public Vim(String type, String name) {
        pluginBroker = new PluginBroker<>();
        try {
            client = pluginBroker.getPlugin("vim-drivers",type,name);
            log.trace("Class is: " + client.getClass().getName());
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        } catch (NotBoundException e) {
            log.error("No plugin found for type: " + type);
        }
    }

    public Vim(String type, String name, int port) {
        pluginBroker = new PluginBroker<>();
        try {
            client = pluginBroker.getPlugin("vim-drivers",type, name, port);
            log.trace("Class is: " + client.getClass().getName());
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        } catch (NotBoundException e) {
            log.error("No plugin found for type: " + type);
        }
    }
}
