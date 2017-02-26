/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.admin;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import javax.validation.Valid;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/datacenters")
public class RestVimInstances {

  //	TODO add log prints
  //	private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VimManagement vimManagement;

  /**
   * Adds a new VNF software Image to the datacenter repository
   *
   * @param vimInstance : Image to add
   * @return datacenter: The datacenter filled with values from the core
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public VimInstance create(
      @RequestBody @Valid VimInstance vimInstance,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException, NotFoundException {

    if (Objects.equals(vimInstance.getName(), "") || vimInstance.getName() == null)
      throw new NotFoundException("The VIM must have an non-empty/null name!");
    if (Objects.equals(vimInstance.getTenant(), "") || vimInstance.getTenant() == null)
      throw new NotFoundException("The VIM must have an non-empty/null tenant!");
    if (Objects.equals(vimInstance.getKeyPair(), ""))
      throw new NotFoundException("The VIM must have an non-empty key pair or null!");
    if (Objects.equals(vimInstance.getUsername(), "") || vimInstance.getUsername() == null)
      throw new NotFoundException("The VIM must have an non-empty/null username!");
    if (vimInstance.getPassword() == null)
      throw new NotFoundException("The VIM must have an non-null password!");
    if (Objects.equals(vimInstance.getType(), "") || vimInstance.getType() == null)
      throw new NotFoundException("The VIM must have an non-empty/null type!");
    return vimManagement.add(vimInstance, projectId);
  }

  /**
   * Removes the Datacenter from the Datacenter repository
   *
   * @param id: The Datacenter's id to be deleted
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadRequestException {
    vimManagement.delete(id, projectId);
  }

  /**
   * Returns the list of the Datacenters available
   *
   * @return List<Datacenter>: The List of Datacenters available
   */
  @RequestMapping(method = RequestMethod.GET)
  public Iterable<VimInstance> findAll(@RequestHeader(value = "project-id") String projectId) {
    Iterable<VimInstance> vimInstances = vimManagement.queryByProjectId(projectId);
    for (VimInstance vim : vimInstances) {
      vim.setPassword("**********");
    }

    return vimInstances;
  }

  /**
   * Returns the Datacenter selected by id
   *
   * @param id: The Datacenter's id selected
   * @return Datacenter: The Datacenter selected
   */
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public VimInstance findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    VimInstance vimInstance = vimManagement.query(id, projectId);
    vimInstance.setPassword("**********");
    return vimInstance;
  }

  /**
   * This operation updates the Network Service Descriptor (NSD)
   *
   * @param new_vimInstance the new datacenter to be updated to
   * @param id the id of the old datacenter
   * @return VimInstance the VimInstance updated
   */
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VimInstance update(
      @RequestBody @Valid VimInstance new_vimInstance,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException {
    return vimManagement.update(new_vimInstance, id, projectId);
  }

  /**
   * Returns the list of NFVImage into a VimInstance with id
   *
   * @param id of the VimInstance
   * @return Set<NFVImage>
   */
  @RequestMapping(value = "{id}/images", method = RequestMethod.GET)
  public Set<NFVImage> getAllImages(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    VimInstance vimInstance = vimManagement.query(id, projectId);
    return vimInstance.getImages();
  }

  /**
   * Returns the {@code NFVImage} selected by idImage from {@code VimInstance} with idVim
   *
   * @param idVim of {@code VimInstance}
   * @param idImage of {@code NFVImage}
   * @return {@code NFVImage} selected
   */
  @RequestMapping(value = "{idVim}/images/{idImage}", method = RequestMethod.GET)
  public NFVImage getImage(
      @PathVariable("idVim") String idVim,
      @PathVariable("idImage") String idImage,
      @RequestHeader(value = "project-id") String projectId)
      throws EntityUnreachableException {
    return vimManagement.queryImage(idVim, idImage, projectId);
  }

  /**
   * Adds a new {@code NFVImage} to the {@code VimInstance} with the id
   *
   * @param id of {@code VimInstance}
   * @param nfvImage the {@code NFVImage} to be added
   * @return {@code NFVImage} persisted
   * @throws VimException
   */
  @RequestMapping(value = "{id}/images", method = RequestMethod.POST)
  public NFVImage addImage(
      @PathVariable("id") String id,
      NFVImage nfvImage,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException {
    return vimManagement.addImage(id, nfvImage, projectId);
  }

  /**
   * Updates the {@code NFVImage} with idImage into {@code VimInstance} with idVim
   *
   * @param idVim of {@code VimInstance}
   * @param image of {@code NFVImage}
   * @return {@code NFVImage} updated
   * @throws VimException
   */
  @RequestMapping(value = "{idVim}/images/{idImage}", method = RequestMethod.PUT)
  public NFVImage updateImage(
      @PathVariable("idVim") String idVim,
      @RequestBody @Valid NFVImage image,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException {
    return vimManagement.addImage(idVim, image, projectId);
  }

  /**
   * Removes the {@code NFVImage} with idImage from {@code VimInstance} with idVim
   *
   * @param idVim of {@code VimInstance}
   * @param idImage of {@code NFVImage}
   * @throws VimException
   */
  @RequestMapping(value = "{idVim}/images/{idImage}", method = RequestMethod.DELETE)
  public void deleteImage(
      @PathVariable("idVim") String idVim,
      @PathVariable("idImage") String idImage,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException {
    vimManagement.deleteImage(idVim, idImage, projectId);
  }

  /**
   * Returns the refreshed Datacenter selected by id
   *
   * @param id: The Datacenter's id selected
   * @return Datacenter: The Datacenter selected
   */
  @RequestMapping(value = "{id}/refresh", method = RequestMethod.GET)
  public VimInstance refresh(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          BadRequestException, AlreadyExistingException {
    VimInstance vimInstance = vimManagement.query(id, projectId);
    vimManagement.refresh(vimInstance);
    return vimInstance;
  }
}
