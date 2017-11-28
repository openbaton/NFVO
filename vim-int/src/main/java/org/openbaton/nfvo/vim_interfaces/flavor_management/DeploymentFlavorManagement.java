/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.vim_interfaces.flavor_management;

import java.util.List;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.VimException;

public interface DeploymentFlavorManagement {
  /**
   * This operation allows adding new DeploymentFlavor to the repository.
   *
   * @param vimInstance The VimInstance to which the deployment flavor will be added @see {@link
   *     BaseVimInstance}
   * @param deploymentFlavour the deployment flavor to add @see {@link DeploymentFlavour}
   * @return the added DeploymentFalvour @see {@link DeploymentFlavour}
   */
  /**
   * * This operation allows adding new DeploymentFlavor to the repository.
   *
   * @param vimInstance The VimInstance to which the deployment flavor will be added @see {@link
   *     BaseVimInstance}
   * @param deploymentFlavour the deployment flavor to add @see {@link DeploymentFlavour}
   * @return the added DeploymentFalvour @see {@link DeploymentFlavour}
   * @throws VimException in case of {@link VimException}
   */
  DeploymentFlavour add(BaseVimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException;

  /**
   * This operation allows deleting in the DeploymentFlavour from the repository.
   *
   * @param vimInstance The VimInstance to which the deployment flavor will be deleted @see {@link
   *     BaseVimInstance}
   * @param deploymentFlavor the deployment flavor to delete @see {@link DeploymentFlavour}
   * @throws VimException in case of {@link VimException}
   */
  void delete(BaseVimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException;

  /**
   * This operation allows updating the DeploymentFlavour in the repository.
   *
   * @param vimInstance The VimInstance to which the deployment flavor will be updated @see {@link
   *     BaseVimInstance}
   * @param deploymentFlavour the deployment flavor to delete @see {@link DeploymentFlavour}
   * @return the updated DeploymentFalvour @see {@link DeploymentFlavour}
   * @throws VimException in case of {@link VimException}
   */
  DeploymentFlavour update(BaseVimInstance vimInstance, DeploymentFlavour deploymentFlavour)
      throws VimException;

  /**
   * This operation allows querying the information of the DeploymentFlavours in the repository.
   *
   * @param vimInstance the vim instance from which gather the list of deployment flavors @see
   *     {@link BaseVimInstance}
   * @return all available DeploymentFalvours @see {@link DeploymentFlavour}
   * @throws VimException in case of {@link VimException}
   */
  List<DeploymentFlavour> queryDeploymentFlavors(BaseVimInstance vimInstance) throws VimException;
}
