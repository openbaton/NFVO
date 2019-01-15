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
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.exceptions.NotFoundException;

/**
 * Used for operations on the NFVImage objects inside the image repository of the NFVO. For
 * NFVImages that are contained in the image repository the isInImageRepo field is set to true.
 */
public interface NfvImageRepoManagement {

  /**
   * This operation allows adding new NFVImages to the image repository.
   *
   * @param nfvImage the new image
   * @return the added NFVImage
   */
  NFVImage add(NFVImage nfvImage);

  /**
   * This method adds an NFVImage to the image repository and stores the actual image file.
   *
   * @param nfvImage
   * @param bytes the image file in bytes
   * @return the added NFVImage
   */
  NFVImage add(NFVImage nfvImage, byte[] bytes) throws IOException;

  /**
   * This method removes an NFVImage from the image repository and checks if the image has the given
   * project ID.
   *
   * @param id ID of the NFVImage to delete
   */
  void delete(String id, String projectId);

  /**
   * This method returns the NFVImages contained in the NFVO's image repository.
   *
   * @return the NFVImages
   */
  Iterable<NFVImage> query();

  /**
   * Returns a single NFVImage from the NFVO's image repository.
   *
   * @param id ID of the NFVImage
   * @return the requested NFVImage
   */
  NFVImage queryById(String id);

  /**
   * This method returns an NFVImage specified by the ID and project ID.
   *
   * @param id ID of the NFVImage
   * @param projectId the project ID
   * @return the requested NFVImage
   */
  NFVImage queryByIdAndProjectId(String id, String projectId);

  /**
   * This method returns all the NFVImages from the image repository with a specific project ID.
   *
   * @param projectId the project ID
   * @return the NFVImages
   */
  Iterable<NFVImage> queryByProjectId(String projectId);

  /**
   * This method returns an NFVImage specified by its name and project ID.
   *
   * @param imageName the NFVImage's name
   * @param projectId the project ID
   * @return the requested NFVImage
   */
  NFVImage queryByNameAndProjectId(String imageName, String projectId);

  /**
   * Returns the URL from which the image file belonging to the passed NFVImage object can be
   * fetched.
   *
   * @param nfvImage an nfvImage which has and image file stored on the local file system
   * @return url pointing to the image file
   */
  String getUrlForLocallyStoredNfvImage(NFVImage nfvImage);

  /**
   * Returns the image file that is associated to an NFVImage as a byte array. This is only possible
   * if the NFVImage is contained in the image repository (isInImageRepo == true) and if it has an
   * image file (storedLocally == true).
   *
   * @param id of the NFVImage
   * @return the image file
   */
  byte[] getImageFileOfNfvImage(String id) throws NotFoundException, IOException;
}
