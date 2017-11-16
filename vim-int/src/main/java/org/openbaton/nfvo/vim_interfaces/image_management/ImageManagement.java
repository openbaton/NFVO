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

package org.openbaton.nfvo.vim_interfaces.image_management;

import java.util.List;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.VimException;

/** Created by mpa on 30/04/15. */
public interface ImageManagement {

  /**
   * This operation allows adding new VNF software images to the image repository.
   *
   * @param vimInstance the {@link BaseVimInstance} to which add the {@link NFVImage}
   * @param image the {@link NFVImage} to add
   * @param imageFile the bytearray containing the image
   * @return the added image @see {@link NFVImage}
   * @throws VimException in case of exception
   */
  NFVImage add(BaseVimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException;

  /**
   * This operation allows adding new VNF software images to the image repository.
   *
   * @param vimInstance the {@link BaseVimInstance} to which add the {@link NFVImage}
   * @param image the {@link NFVImage} to add
   * @param image_url the link hosting the image
   * @return the added image @see {@link NFVImage}
   * @throws VimException in case of exception
   */
  NFVImage add(BaseVimInstance vimInstance, NFVImage image, String image_url) throws VimException;

  /**
   * This operation allows deleting in the VNF software images from the image repository.
   *
   * @param vimInstance the {@link BaseVimInstance} to which remove the {@link NFVImage}
   * @param image the {@link NFVImage} to remove
   * @throws VimException in case of exception
   */
  void delete(BaseVimInstance vimInstance, NFVImage image) throws VimException;

  /**
   * This operation allows updating the VNF software images in the image repository.
   *
   * @param vimInstance the {@link BaseVimInstance} where update the {@link NFVImage}
   * @param image the {@link NFVImage} to update
   * @return the updated {@link NFVImage}
   * @throws VimException in case of exception
   */
  BaseNfvImage update(BaseVimInstance vimInstance, NFVImage image) throws VimException;

  /**
   * This operation allows querying the information of the VNF software images in the image
   * repository.
   *
   * @param vimInstance the {@link BaseVimInstance} to which request the list of images
   * @return the list of available images
   * @throws VimException in case of exception
   */
  List<BaseNfvImage> queryImages(BaseVimInstance vimInstance) throws VimException;

  /**
   * This operation allows copying images from a VIM to another.
   *
   * @param vimInstance the {@link BaseVimInstance} to which copy the image
   * @param image the {@link NFVImage} to be copied
   * @param imageFile the bytearray containing the image
   * @throws VimException in case of exception
   */
  void copy(BaseVimInstance vimInstance, NFVImage image, byte[] imageFile) throws VimException;
}
