/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Future;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;

public interface VimManagement {

  /** This operation allows adding a datacenter into the datacenter repository. */
  Future<BaseVimInstance> add(BaseVimInstance vimInstance, String projectId)
      throws VimException, PluginException, IOException, BadRequestException;

  /** This operation allows deleting the datacenter from the datacenter repository. */
  void delete(String id, String projectId) throws NotFoundException, BadRequestException;

  /** This operation allows updating the datacenter in the datacenter repository. */
  Future<BaseVimInstance> update(BaseVimInstance new_vimInstance, String id, String projectId)
      throws VimException, PluginException, IOException, BadRequestException, NotFoundException;

  /**
   * This operation allows querying the information of the datacenter in the datacenter repository.
   */
  BaseVimInstance query(String id, String projectId);

  Future<BaseVimInstance> refresh(BaseVimInstance vimInstance, boolean force)
      throws VimException, PluginException, IOException;

  /**
   * Adds a new NFVImage to the VimInstance with id
   *
   * @param id of VimInstance
   * @param image the new NFVImage
   * @return NFVImage
   */
  BaseNfvImage addImage(String id, BaseNfvImage image, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          NotFoundException;

  /**
   * Returns the NFVImage with idImage from VimInstance with idVim
   *
   * @return NFVImage
   */
  BaseNfvImage queryImage(String idVim, String idImage, String projectId)
      throws EntityUnreachableException, NotFoundException;

  /** Removes the NFVImage with idImage from VimInstance with idVim */
  void deleteImage(String idVim, String idImage, String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          NotFoundException;

  Iterable<BaseVimInstance> queryByProjectId(String projectId);

  void checkVimInstances() throws IOException;

  Set<BaseNfvImage> queryImagesDirectly(BaseVimInstance vimInstance)
      throws PluginException, VimException;

  Future<Void> deleteNetwork(VirtualLinkRecord vlr)
      throws PluginException, NotFoundException, VimException;

  BaseVimInstance query(String vim_id);

  BaseVimInstance queryByProjectIdAndName(String projectId, String name) throws NotFoundException;
}
