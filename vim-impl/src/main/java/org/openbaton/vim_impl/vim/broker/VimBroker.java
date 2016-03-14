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

package org.openbaton.vim_impl.vim.broker;

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.vim.drivers.interfaces.ClientInterfaces;
import org.openbaton.vim_impl.vim.AmazonVIM;
import org.openbaton.vim_impl.vim.OpenstackVIM;
import org.openbaton.vim_impl.vim.TestVIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lto on 20/05/15.
 */
@Service
@Scope
@ConfigurationProperties(prefix = "nfvo.rabbit.management")
public class VimBroker implements org.openbaton.nfvo.vim_interfaces.vim.VimBroker {

    private String port;
    @Value("${nfvo.vim.drivers.allowInfiniteQuota:}")
    private String allowInfiniteQuota;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ConfigurableApplicationContext context;
    private HashMap<String, ClientInterfaces> clientInterfaces;

    public String getAllowInfiniteQuota() {
        return allowInfiniteQuota;
    }

    public void setAllowInfiniteQuota(String allowInfiniteQuota) {
        this.allowInfiniteQuota = allowInfiniteQuota;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @PostConstruct
    private void init() {
        log.debug("MANAGEMENT_PORT is: " + port);
        if (port == null) {
            port = "15672";
        }
        this.clientInterfaces = new HashMap<>();
    }

    @Override
    public void addClient(ClientInterfaces client, String type) {
        log.info("Registered client of type: " + type);
        this.clientInterfaces.put(type, client);
    }

    @Override
    public ClientInterfaces getClient(String type) {
        return this.clientInterfaces.get(type);
    }

    @Override
    public Vim getVim(String type, String name) {
        switch (type) {
            case "test":
                return (Vim) context.getBean("testVIM", type, name, this.port);
            case "openstack":
                return (Vim) context.getBean("openstackVIM", type, name, this.port, context);
            case "amazon":
                return (Vim) context.getBean("amazonVIM", type, name, this.port);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Vim getVim(String type) throws PluginException {
        switch (type) {
            case "test":
//                return (Vim) context.getBean("testVIM", this.port);
                return new TestVIM(this.port);
            case "openstack":
//                return (Vim) context.getBean("openstackVIM", this.port, context);
                return new OpenstackVIM(this.port, context);
            case "amazon":
//                return (Vim) context.getBean("amazonVIM", this.port);
                new AmazonVIM(this.port);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Vim getVim(String type, int port) {
        switch (type) {
            case "test":
                return (Vim) context.getBean("testVIM", port, this.port);
            case "openstack":
                return (Vim) context.getBean("openstackVIM", port, this.port, context);
            case "amazon":
                return (Vim) context.getBean("amazonVIM", port, this.port);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Vim getVim(String type, String name, String port) {
        switch (type) {
            case "test":
                return (Vim) context.getBean("testVIM", type, name, Integer.parseInt(port), this.port);
            case "openstack":
                return (Vim) context.getBean("openstackVIM", type, name, Integer.parseInt(port), this.port, context);
            case "amazon":
                return (Vim) context.getBean("amazonVIM", type, name, Integer.parseInt(port), this.port);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Quota getLeftQuota(VimInstance vimInstance) throws VimException, PluginException {
        Vim vim = getVim(vimInstance.getType());

        Quota maximalQuota = vim.getQuota(vimInstance);

        if (allowInfiniteQuota != null && allowInfiniteQuota.equalsIgnoreCase("true")) {
            if (maximalQuota.getInstances() == -1) {
                maximalQuota.setInstances(Integer.MAX_VALUE);
            }
            if (maximalQuota.getRam() == -1) {
                maximalQuota.setRam(Integer.MAX_VALUE);
            }
            if (maximalQuota.getCores() == -1) {
                maximalQuota.setCores(Integer.MAX_VALUE);
            }
            if (maximalQuota.getKeyPairs() == -1) {
                maximalQuota.setKeyPairs(Integer.MAX_VALUE);
            }
            if (maximalQuota.getFloatingIps() == -1) {
                maximalQuota.setFloatingIps(Integer.MAX_VALUE);
            }
        } else {
            if (maximalQuota.getInstances() < 0 || maximalQuota.getRam() < 0 || maximalQuota.getCores() < 0 || maximalQuota.getKeyPairs() < 0 || maximalQuota.getFloatingIps() < 0) {
                log.error("Infinite quota are not allowed. Please set nfvo.vim.drivers.allowInfiniteQuota to true or change the quota in your VIM installation");
                throw new VimException("Infinite quota are not allowed. Please set nfvo.vim.drivers.allowInfiniteQuota to true or change the quota in your VIM installation");
            }
        }
        Quota leftQuota = maximalQuota;

        List<Server> servers = vim.queryResources(vimInstance);
        //Calculate used resource by servers (cpus, ram)
        for (Server server : servers) {
            //Subtract floatingIps

            //Subtract instances
            leftQuota.setInstances(leftQuota.getInstances() - 1);
            //Subtract used ram and cpus
            // TODO check whenever the library/rest command work.
            DeploymentFlavour flavor = server.getFlavor();
            leftQuota.setRam(leftQuota.getRam() - flavor.getRam());
            leftQuota.setCores(leftQuota.getCores() - flavor.getVcpus());
            // TODO add floating ips when quota command will work...
        }
        return leftQuota;
    }
}
