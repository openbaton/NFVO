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

import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Server;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.project.openbaton.nfvo.exceptions.VimException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 11/06/15.
 */
@Service
@Scope
public class ResourceManagement implements org.project.openbaton.nfvo.core.interfaces.ResourceManagement {
    @Autowired
    private VimBroker vimBroker;

    @Override
    public Future<String> allocate(VirtualDeploymentUnit virtualDeploymentUnit, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimException, VimDriverException {
        org.project.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim;
        vim = vimBroker.getVim(virtualDeploymentUnit.getVimInstance().getType());
        return vim.allocate(virtualDeploymentUnit,virtualNetworkFunctionRecord);
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
    public void release(VirtualDeploymentUnit virtualDeploymentUnit) throws VimException {
        org.project.openbaton.nfvo.vim_interfaces.resource_management.ResourceManagement vim = vimBroker.getVim(virtualDeploymentUnit.getVimInstance().getType());
        vim.release(virtualDeploymentUnit);
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
