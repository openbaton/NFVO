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

package org.project.openbaton.nfvo.vim;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.catalogue.util.IdGenerator;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.PluginInvokeException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.plugin.concretes.ClientInterfacePluginAgent;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class TestVIM implements Vim {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClientInterfacePluginAgent testClient;

    @Autowired
    private VimBroker vimBroker;

    @PostConstruct
    private void init() {
//        this.testClient = vimBroker.getClient("test");
//        if (testClient ==null) {
//            log.error("Plugin Test Vim Drivers not found. Have you installed it?");
//            NotFoundException notFoundException = new NotFoundException("Plugin Test Vim Drivers not found. Have you installed it?");
//            throw new RuntimeException(notFoundException);
//        }
    }

    @Override
    public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException {

    }

    @Override
    public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) throws VimException {
        try {
            return testClient.listFlavors(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
    }


    @Override
    public NFVImage add(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {
        try {
            return this.testClient.addImage(vimInstance.getType(), vimInstance, image,inputStream);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
    }

    @Override
    public void delete(VimInstance vimInstance, NFVImage image) throws VimException {

    }

    @Override
    public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NFVImage> queryImages(VimInstance vimInstance) throws VimException {
        try {
            return testClient.listImages(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
    }

    @Override
    public void copy(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {

    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimDriverException, VimException {
        VimInstance vimInstance = vdu.getVimInstance();
        log.trace("Initializing " + vimInstance);
        try {
            HashSet<String> networks = new HashSet<>();
            networks.add("network_id");
            HashSet<String> securityGroups = new HashSet<String>();
            securityGroups.add("secGroup_id");
            testClient.launchInstanceAndWait(vimInstance.getType(), vimInstance,vdu.getHostname(),vimInstance.getImages().iterator().next().getExtId(),"flavor","keypair", networks, securityGroups, "#userdate");
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
        String id = IdGenerator.createUUID();
        log.debug("launched instance with id " + id);
        return new AsyncResult<String>(id);
    }

    @Override
    public List<Server> queryResources(VimInstance vimInstance) throws VimException {

        try {
            return testClient.listServer(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
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
    @Async
    public Future<Void> release(VirtualDeploymentUnit vdu, VimInstance vimInstance) {
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

    @Override
    public Network add(VimInstance vimInstance, Network network) throws VimException {
        return network;
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException {

    }

    @Override
    public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
        try {
            return this.testClient.listNetworks(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
    }

    @Override
    public Network query(VimInstance vimInstance, String extId) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Quota getQuota(VimInstance vimInstance) throws VimException {
        try {
            return this.testClient.getQuota(vimInstance.getType(), vimInstance);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new VimException(e);
        } catch (PluginInvokeException e) {
            e.printStackTrace();
            throw new VimException(e);
        }
    }
}
