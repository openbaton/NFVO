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

import org.openbaton.catalogue.nfvo.NFVImage;

/** Created by mpa on 30/04/15. */
public interface NFVImageManagement {

  /** This operation allows adding new VNF software images to the image repository. */
  NFVImage add(NFVImage NFVImage);

  /** This operation allows deleting in the VNF software images from the image repository. */
  void delete(String id);

  /** This operation allows updating the VNF software images in the image repository. */
  NFVImage update(NFVImage new_NFV_image, String id);

  /**
   * This operation allows querying the information of the VNF software images in the image
   * repository.
   */
  Iterable<NFVImage> query();

  /**
   * This operation allows querying the information of the VNF software image in the image
   * repository.
   */
  NFVImage query(String id);

  /** This operation allows copying images from a VIM to another. */
  void copy();
}
