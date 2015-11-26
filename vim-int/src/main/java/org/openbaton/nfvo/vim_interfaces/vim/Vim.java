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

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.nfvo.vim_interfaces.flavor_management.DeploymentFlavorManagement;
import org.openbaton.nfvo.vim_interfaces.image_management.ImageManagement;
import org.openbaton.nfvo.vim_interfaces.network_management.NetworkManagement;
import org.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement;
import org.openbaton.vim.drivers.VimDriverCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by mpa on 12.06.15.
 */
@Service
@Scope("prototype")
public abstract class Vim implements ImageManagement, ResourceManagement, NetworkManagement, DeploymentFlavorManagement {
    protected Logger log = LoggerFactory.getLogger(this.getClass());


    protected VimDriverCaller client;

    public Vim(String type, String brokerIp, int port) throws PluginException {
        try {
//            client = (VimDriverCaller) RabbitPluginBroker.getVimDriverCaller(brokerIp, port, type);
            client = new VimDriverCaller(brokerIp, port, type);
        } catch (TimeoutException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        }
    }

    public Vim(String type) throws PluginException {
        try {
//            client = (VimDriverCaller) RabbitPluginBroker.getVimDriverCaller(type);
            client = new VimDriverCaller(type);
        } catch (TimeoutException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        }
    }

    public Vim(String type, String name) throws PluginException {
        try {
//            client = (VimDriverCaller) RabbitPluginBroker.getVimDriverCaller(name, type);
            client = new VimDriverCaller(name, type);
        } catch (TimeoutException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        }
    }

    public Vim(String type, String username, String password, String brokerIp) throws PluginException {
        try {
//            client = (VimDriverCaller) RabbitPluginBroker.getVimDriverCaller(brokerIp,username,password,type);
            client = new VimDriverCaller(brokerIp,username,password,type);
        } catch (TimeoutException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new PluginException("Error instantiating plugin: " + e.getMessage(), e);
        }
    }

    public VimDriverCaller getClient() {
        return client;
    }

    public void setClient(VimDriverCaller client) {
        this.client = client;
    }
}
