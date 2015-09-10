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

package org.project.openbaton.nfvo.vim.broker;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.nfvo.Quota;
import org.project.openbaton.catalogue.nfvo.Server;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.exceptions.VimException;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class VimBroker implements org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurableApplicationContext context;

    private HashMap<String, ClientInterfaces> clientInterfaces;

    @PostConstruct
    private void init(){
        this.clientInterfaces = new HashMap<>();
    }

    @Override
    public void addClient(ClientInterfaces client, String type){
        log.info("Registered client of type: " + type);
        this.clientInterfaces.put(type, client);
    }

    @Override
    public ClientInterfaces getClient(String type){
        return this.clientInterfaces.get(type);
    }

    @Override
    public Vim getVim(String type, String name, String port) {
        switch (type) {
            case "test":
                return (Vim) context.getBean("testVIM", name, Integer.parseInt(port));
            case "openstack":
                return (Vim) context.getBean("openstackVIM", name, Integer.parseInt(port));
            case "amazon":
                return (Vim) context.getBean("amazonVIM", name, Integer.parseInt(port));
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Quota getLeftQuota(VimInstance vimInstance) throws VimException {
        Vim vim = getVim(vimInstance.getType(), vimInstance.getType(), "1099");

        Quota maximalQuota = vim.getQuota(vimInstance);
        Quota leftQuota = maximalQuota;

        List<Server> servers = vim.queryResources(vimInstance);
        //Calculate used resource by servers (cpus, ram)
        for (Server server : servers) {
            //Subtract floatingIps

            //Subtract instances
            leftQuota.setInstances(leftQuota.getInstances()-1);
            //Subtract used ram and cpus
            // TODO check whenever the library/rest command work.
            DeploymentFlavour flavor = server.getFlavor();
            leftQuota.setRam(leftQuota.getRam()-flavor.getRam());
            leftQuota.setCores(leftQuota.getCores()-flavor.getVcpus());
            // TODO add floating ips when quota command will work...
        }
        return leftQuota;
    }
}
