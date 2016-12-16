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

package org.openbaton.nfvo.core.interfaces;

import java.io.IOException;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.*;

/** Created by lto on 13/05/15. */
public interface VimManagement {

  /** This operation allows adding a datacenter into the datacenter repository. */
  VimInstance add(VimInstance vimInstance, String projectId)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException;

  /** This operation allows deleting the datacenter from the datacenter repository. */
  void delete(String id, String projectId) throws NotFoundException, BadRequestException;

  /** This operation allows updating the datacenter in the datacenter repository. */
  VimInstance update(VimInstance new_vimInstance, String id, String projectId)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException;

  /**
   * This operation allows querying the information of the datacenter in the datacenter repository.
   */
  VimInstance query(String id, String projectId);

  VimInstance refresh(VimInstance vimInstance)
      throws VimException, PluginException, IOException, BadRequestException,
          AlreadyExistingException;

  /**
   * Adds a new NFVImage to the VimInstance with id
   *
   * @param id of VimInstance
   * @param image the new NFVImage
   * @return NFVImage
   */
  NFVImage addImage(String id, NFVImage image, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException;

  /**
   * Returns the NFVImage with idImage from VimInstance with idVim
   *
   * @return NFVImage
   */
  NFVImage queryImage(String idVim, String idImage, String projectId)
      throws EntityUnreachableException;

  /** Removes the NFVImage with idImage from VimInstance with idVim */
  void deleteImage(String idVim, String idImage, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException;

  Iterable<VimInstance> queryByProjectId(String projectId);
}
