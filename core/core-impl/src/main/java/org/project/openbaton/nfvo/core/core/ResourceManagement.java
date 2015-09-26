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

package org.project.openbaton.nfvo.core.core;

import org.project.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Server;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.exceptions.VimException;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by lto on 11/06/15.
 */
@Service
@Scope("prototype")
public class ResourceManagement implements org.project.openbaton.nfvo.core.interfaces.ResourceManagement {

    private static final String gitRepoEms = "https://gitlab.fokus.fraunhofer.de/openbaton/ems-public.git";
    private static final String branch = "develop";

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private VimBroker vimBroker;

    @Override
    public List<String> allocate(VirtualDeploymentUnit virtualDeploymentUnit, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimException, VimDriverException, ExecutionException, InterruptedException {
        org.project.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim;
        vim = vimBroker.getVim(virtualDeploymentUnit.getVimInstance().getType());
        log.debug("Executing allocate with Vim: " + vim.getClass().getSimpleName());
        List<String> ids = new ArrayList<>();
        log.debug("NAME: " + virtualNetworkFunctionRecord.getName());
        log.debug("ID: " + virtualDeploymentUnit.getId());
        virtualDeploymentUnit.setHostname(virtualNetworkFunctionRecord.getName());
        for (VNFComponent component : virtualDeploymentUnit.getVnfc()) {
            log.trace("UserData is: " + getUserData(virtualNetworkFunctionRecord.getEndpoint()));
            log.debug("The component is Exposed? " + component.isExposed());
            VNFCInstance added = vim.allocate(virtualDeploymentUnit, virtualNetworkFunctionRecord, component, getUserData(virtualNetworkFunctionRecord.getEndpoint()), component.isExposed()).get();
            ids.add(added.getVc_id());
            if (component.isExposed() && added.getFloatingIps().size() == 0)
                log.warn("NFVO wasn't able to associate FloatingIPs. Is there enough available");
        }
        return ids;
    }

    private String getUserData(String endpoint) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("/etc/openbaton/openbaton.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.debug("Loaded: " + properties);
        String url = properties.getProperty("spring.activemq.broker-url");
        String activeIp = (String) url.subSequence(6, url.indexOf(":61616"));
        log.debug("Active ip is: " + activeIp);
        String result = "#!/bin/bash\n" +
                "apt-get update\n" +
                "apt-get install -y git python-pip\n" +
                "git clone " + gitRepoEms + " -b " + branch + " /opt/ems-deb\n" +
                "dpkg -i /opt/ems-deb/ems_1.0-1.deb\n" +
                "echo [ems] > /etc/openbaton/ems/conf.ini\n" +
                "echo orch_ip=" + activeIp + " >> /etc/openbaton/ems/conf.ini\n" +
                "export hn=`hostname`\n" +
                "echo \"type=" + endpoint + "\" >> /etc/openbaton/ems/conf.ini\n" +
                "echo \"hostname=$hn\" >> /etc/openbaton/ems/conf.ini\n" +
                "echo orch_port=61613 >> /etc/openbaton/ems/conf.ini\n" +

                "sudo /opt/openbaton/ems/ems.sh start\n";
        return result;
    }

    @Override
    public List<Server> query(VimInstance vimInstance) throws VimException {
        return vimBroker.getVim(vimInstance.getType()).queryResources(vimInstance);
    }

    @Override
    public void update(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void scale(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void migrate(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void operate(VirtualDeploymentUnit vdu, String operation) {

    }

    @Override
    public Future<Void> release(VirtualDeploymentUnit virtualDeploymentUnit, VNFCInstance vnfcInstance) throws VimException, ExecutionException, InterruptedException {
        org.project.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim = vimBroker.getVim(virtualDeploymentUnit.getVimInstance().getType());
        log.debug("Removing vnfcInstance: " + vnfcInstance);
        vim.release(vnfcInstance, virtualDeploymentUnit.getVimInstance()).get();
        return new AsyncResult<>(null);
    }

    @Override
    public void createReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void queryReservation() {

    }

    @Override
    public void updateReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void releaseReservation(VirtualDeploymentUnit vdu) {

    }
}
