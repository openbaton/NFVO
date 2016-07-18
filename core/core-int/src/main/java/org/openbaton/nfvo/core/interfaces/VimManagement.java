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

import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;

import java.io.IOException;

/**
 * Created by lto on 13/05/15.
 */
public interface VimManagement {

  /**
   * This operation allows adding a datacenter into the datacenter repository.
   *
   * @param vimInstance
   * @param projectId
   */
  VimInstance add(VimInstance vimInstance, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException;

  /**
   * This operation allows deleting the datacenter from the datacenter repository.
   *
   * @param id
   * @param projectId
   */
  void delete(String id, String projectId);

  /**
   * This operation allows updating the datacenter in the datacenter repository.
   *
   * @param new_vimInstance
   * @param id
   * @param projectId
   */
  VimInstance update(VimInstance new_vimInstance, String id, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException;

  /**
   * This operation allows querying the information of the datacenter in the datacenter repository.
   */
  VimInstance query(String id, String projectId);

  void refresh(VimInstance vimInstance) throws VimException, PluginException, IOException;

  /**
   * Adds a new NFVImage to the VimInstance with id
   *
   * @param id of VimInstance
   * @param image the new NFVImage
   * @param projectId
   * @return NFVImage
   */
  NFVImage addImage(String id, NFVImage image, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException;

  /**
   * Returns the NFVImage with idImage from VimInstance with idVim
   *
   * @param idVim
   * @param idImage
   * @param projectId
   * @return NFVImage
   */
  NFVImage queryImage(String idVim, String idImage, String projectId)
      throws EntityUnreachableException;

  /**
   * Removes the NFVImage with idImage from VimInstance with idVim
   *
   * @param idVim
   * @param idImage
   * @param projectId
   */
  void deleteImage(String idVim, String idImage, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException;

  Iterable<VimInstance> queryByProjectId(String projectId);
}
