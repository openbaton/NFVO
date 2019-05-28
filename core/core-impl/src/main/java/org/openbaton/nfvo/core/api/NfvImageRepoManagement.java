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

package org.openbaton.nfvo.core.api;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.NFVImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope
public class NfvImageRepoManagement
    implements org.openbaton.nfvo.core.interfaces.NfvImageRepoManagement {

  @Value("${nfvo.nfvimagedir.path:/etc/openbaton/nfvImages/}")
  private String nfvImageDirPath;

  @Value("${nfvo.server.ip:localhost}")
  private String serverIp;

  @Value("${server.port:8080}")
  private String serverPort;

  @Value("${server.ssl.enabled:false}")
  private boolean tlsEnabled;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NFVImageRepository nfvImageRepository;

  @Override
  public String getUrlForLocallyStoredNfvImage(NFVImage nfvImage) {
    if (nfvImage == null || !nfvImage.isStoredLocally())
      throw new IllegalArgumentException(
          "The passed NFVImage is either null or not stored on the local file system");
    return "http"
        + (tlsEnabled ? "s" : "")
        + "://"
        + serverIp
        + ":"
        + serverPort
        + "/api/v1/nfvimage/file/"
        + nfvImage.getId();
  }

  @Override
  public NFVImage add(NFVImage nfvImage, String projectId) throws AlreadyExistingException {
    if (queryByNameAndProjectId(nfvImage.getName(), projectId) != null)
      throw new AlreadyExistingException(
          "An image with name "
              + nfvImage.getName()
              + " exists already in the "
              + "image repository for project "
              + projectId);
    if (!nfvImage.isInImageRepo())
      throw new IllegalArgumentException(
          "The 'isInImageRepo' field of the NFVImage to add is set to false but must be true");
    log.debug("Adding image with name " + nfvImage.getName());
    if (nfvImage.isStoredLocally()) nfvImage.setUrl(null);
    nfvImage.setCreated(new Date());
    nfvImage = nfvImageRepository.save(nfvImage);
    return nfvImage;
  }

  /**
   * Appends a slash to the String if it does not yet end with one.
   *
   * @return the String with a trailing slash
   */
  private String ensureTrailingSlash(String s) {
    if (!s.endsWith("/")) return s + "/";
    return s;
  }

  @Override
  public NFVImage add(NFVImage nfvImage, byte[] bytes, String projectId)
      throws IOException, AlreadyExistingException {
    if (queryByNameAndProjectId(nfvImage.getName(), projectId) != null)
      throw new AlreadyExistingException(
          "An image with name "
              + nfvImage.getName()
              + " exists already in the "
              + "image repository for project "
              + projectId);
    if (!nfvImage.isInImageRepo())
      throw new IllegalArgumentException(
          "The 'isInImageRepo' field of the NFVImage to add is set to false but must be true");
    if (!Files.isDirectory(Paths.get(nfvImageDirPath))) {
      if (Files.exists(Paths.get(nfvImageDirPath)))
        throw new FileAlreadyExistsException(
            "The nfvImage directory cannot be created because there is already a file at "
                + nfvImageDirPath);
      Files.createDirectory(Paths.get(nfvImageDirPath));
    }
    nfvImage.setUrl(null);
    nfvImage.setStoredLocally(true);
    nfvImage.setCreated(new Date());
    nfvImage = nfvImageRepository.save(nfvImage);
    try {
      File imageFile = new File(ensureTrailingSlash(nfvImageDirPath) + nfvImage.getId());
      FileUtils.writeByteArrayToFile(imageFile, bytes);
      try {
        nfvImage.setHash(new DigestUtils("MD5").digestAsHex(imageFile));
        nfvImage = nfvImageRepository.save(nfvImage);
      } catch (Exception e) {
        log.error(
            "Unable to calculate or save MD5 hash for nfvImage "
                + nfvImage.getName()
                + ": "
                + e.getMessage());
        // The exception is ignored so that the NFVImage does not have an MD5 checksum
      }
    } catch (IOException e) {
      try {
        nfvImageRepository.delete(nfvImage);
      } catch (Exception e1) {
        log.error(
            "Unable to delete the nfvImage " + nfvImage.getName() + " from nfvImage repository");
      }
      throw new IOException(
          "Unable to save image file for NFVImage " + nfvImage.getName() + ": " + e.getMessage(),
          e);
    }
    return nfvImage;
  }

  /**
   * Tries to delete the file associated to an NFVImage from the file system. It does not throw an
   * exception if the removal fails.
   *
   * @param name name of the file (currently the NFVImage's ID)
   */
  private void deleteNfvImageFile(String name) {
    Path filePath = Paths.get(ensureTrailingSlash(nfvImageDirPath) + name);
    if (Files.exists(filePath)) {
      try {
        Files.delete(filePath);
      } catch (IOException e) {
        log.error(
            "Exception while deleting the nfvImage file "
                + filePath.toString()
                + ": "
                + e.getMessage());
      }
    }
  }

  /**
   * Delete an NFVImage.
   *
   * @param id the ID of the NFVImage to delete
   * @param projectId the ID of the project to which the NFVImage belongs
   */
  @Override
  public void delete(String id, String projectId) {
    log.debug(
        "Removing image with ID " + id + " from image repository (project ID: " + projectId + ")");
    if (nfvImageRepository.findOneByIdAndProjectIdAndIsInImageRepoIsTrue(id, projectId) != null) {
      deleteNfvImageFile(id);
      nfvImageRepository.delete(id);
    } else {
      log.warn(
          "NFVImage with ID "
              + id
              + " could not be deleted because it was not found in project with ID "
              + projectId);
    }
  }

  @Override
  public Iterable<NFVImage> query() {
    return nfvImageRepository.isInImageRepoIsTrue();
  }

  @Override
  public NFVImage queryById(String id) {
    return nfvImageRepository.findOneByIdAndIsInImageRepoIsTrue(id);
  }

  @Override
  public NFVImage queryByIdAndProjectId(String id, String projectId) {
    return nfvImageRepository.findOneByIdAndProjectIdAndIsInImageRepoIsTrue(id, projectId);
  }

  @Override
  public Iterable<NFVImage> queryByProjectId(String projectId) {
    return nfvImageRepository.findAllByProjectIdAndIsInImageRepoIsTrue(projectId);
  }

  @Override
  public NFVImage queryByNameAndProjectId(String imageName, String projectId) {
    return nfvImageRepository.findOneByNameAndProjectIdAndIsInImageRepoIsTrue(imageName, projectId);
  }

  @Override
  public byte[] getImageFileOfNfvImage(String id) throws NotFoundException, IOException {
    NFVImage image = this.queryById(id);
    if (image == null)
      throw new NotFoundException("No NFVImage with ID " + id + " found in image repository");
    if (!image.isInImageRepo())
      throw new NotFoundException(
          "Did not find an NFVImage with ID " + id + " in the image repository");

    if (!image.isStoredLocally())
      throw new NotFoundException(
          "The NFVImage with ID " + id + " does not have an associated image file");

    String filePath = ensureTrailingSlash(nfvImageDirPath) + image.getId();
    if (!Files.exists(Paths.get(filePath)))
      throw new NotFoundException("Not found the image file to serve for NFVImage with ID " + id);
    File imageFile = new File(filePath);
    InputStream inputStream = FileUtils.openInputStream(imageFile);
    return IOUtils.toByteArray(inputStream);
  }
}
