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

package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.VimDriverException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by mpa on 30/04/15.
 */

public interface ResourceManagement {
    /**
     * This operation allows requesting the instantiation and
     * assignment of a virtualised resource to the VNF, as
     * indicated by the consumer functional block.
     */
    List<String> allocate(VirtualDeploymentUnit virtualDeploymentUnit, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimException, VimDriverException, ExecutionException, InterruptedException, VimException;

    /**
     * This operation allows querying a virtualised resource,
     * i.e. retrieve information about an instantiated virtualised
     * resource.
     *
     * @param vimInstance
     */
    List<Server> query(VimInstance vimInstance) throws VimException;

    /**
     * This operation allows updating the configuration and/or
     * parameterization of an instantiated virtualised resource.
     */
    void update(VirtualDeploymentUnit vdu);

    /**
     * This operation allows scaling a virtualised resource by
     * adding or removing capacity, e.g. adding vCPUs to a
     * virtual machine.
     */
    void scale(VirtualDeploymentUnit vdu);

    /**
     * This operation allows moving virtualised resources
     * between locations. For instance, the operation performs
     * the migration of a computing resource from one host to
     * another host; while for a storage resource, it migrates
     * the resource from one storage location to another.
     */
    void migrate(VirtualDeploymentUnit vdu);

    /**
     * This operation allows executing specific commands on
     * certain allocated virtualised resources. Examples on
     * compute resources can include (but not limited to): start,
     * stop, pause, suspend, capture snapshot, etc.
     */
    void operate(VirtualDeploymentUnit vdu, String operation);

    /**
     * This operation allows de-allocating and terminating an
     * instantiated virtualised resource. This operation frees
     * resources and returns them to the NFVI resource pool.
     */
    Future<Void> release(VirtualDeploymentUnit vdu, VNFCInstance vnfcInstance) throws VimException, ExecutionException, InterruptedException;

    /**
     * This operation allows requesting the reservation of a set
     * of virtualised resources to a consumer functional block
     * without performing the steps of "Allocate Resource".
     */
    void createReservation(VirtualDeploymentUnit vdu);

    /**
     * This operation allows querying an issued resources
     * reservation, e.g. to discover the virtualised resources
     * included in a specific reserved resources pool, or the
     * amount of free resources in such a pool.
     */
    void queryReservation();

    /**
     * This operation allows updating an issued resources
     * reservation to increase or decrease the amount of
     * virtualised resources in the reserved resources pool.
     */
    void updateReservation(VirtualDeploymentUnit vdu);

    /**
     * This operation allows releasing an issued resources
     * reservation, hence freeing the reserved virtualised
     * resources.
     */
    void releaseReservation(VirtualDeploymentUnit vdu);

    String allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFComponent componentToAdd) throws InterruptedException, ExecutionException, VimException, VimDriverException;
}
