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

package org.openbaton.nfvo.vim_interfaces.flavor_management;

import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.VimException;

import java.util.List;

/**
 * Created by lto on 03/06/15.
 */
public interface DeploymentFlavorManagement {
    /**
     * This operation allows adding new DeploymentFlavor
     * to the repository.
     * @param vimInstance
     * @param deploymentFlavour
     */
    DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException;

    /**
     * This operation allows deleting in the DeploymentFlavour
     * from the repository.
     * @param vimInstance
     * @param deploymentFlavor
     */
    void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException;

    /**
     * This operation allows updating the DeploymentFlavour
     * in the repository.
     * @param vimInstance
     * @param deploymentFlavour
     */
    DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException;

    /**
     * This operation allows querying the information of
     * the DeploymentFlavours in the repository.
     * @param vimInstance
     */
    List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) throws VimException;

}
