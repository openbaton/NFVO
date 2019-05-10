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

package org.openbaton.nfvo.api.admin;

import static org.openbaton.nfvo.common.utils.viminstance.VimInstanceUtils.handlePrivateInfo;

import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.core.interfaces.ComponentManager;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/datacenters")
public class RestVimInstances {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VimManagement vimManagement;
  @Autowired private ComponentManager componentManager;

  /**
   * Adds a new VNF software Image to the datacenter repository
   *
   * @param vimInstance : Image to add
   * @return datacenter: The datacenter filled with values from the core
   */
  @ApiOperation(
      value = "Adding a Vim Instance",
      notes = "Takes a Vim Instance json in the request body")
  @RequestMapping(
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public BaseVimInstance create(
      @RequestBody @Valid BaseVimInstance vimInstance,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, IOException, BadRequestException, ExecutionException,
          InterruptedException {
    log.info("Adding vim instance: " + vimInstance);
    return vimManagement.add(vimInstance, projectId).get();
  }

  /**
   * Removes the Datacenter from the Datacenter repository
   *
   * @param id: The Datacenter's id to be deleted
   */
  @ApiOperation(
      value = " Removing a Vim Instance",
      notes = "Deletes the Vim Instance belonging to the id specified in the URL")
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadRequestException {
    log.info("Deleting vim instance id: " + id);
    vimManagement.delete(id, projectId);
  }

  /**
   * Removes multiple VIM Instances
   *
   * @param ids: the list of VIM Instance IDs
   * @throws NotFoundException if one of the VIM Instances was not found
   * @throws BadRequestException if something is wrong with the request
   */
  @RequestMapping(
      value = "/multipledelete",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(
      value = "Removing multiple VIM Instances",
      notes = "Delete Request takes a list of VIM Instance IDs")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadRequestException {
    log.info("Deleting vim instance ids: " + ids);
    for (String id : ids) vimManagement.delete(id, projectId);
  }

  /**
   * Returns the list of the Datacenters available
   *
   * @return List<Datacenter>: The List of Datacenters available
   */
  @ApiOperation(
      value = "Retrieving all Vim Instances",
      notes =
          "This method returns the list of all the VimInstances on-boarded in this project identified by the header project-id")
  @RequestMapping(method = RequestMethod.GET)
  public List<BaseVimInstance> findAll(
      @RequestHeader(value = "project-id") String projectId,
      @RequestHeader(value = "authorization") String token)
      throws BadFormatException {
    String[] tokenArray = token.split(" ");
    if (tokenArray.length < 2) throw new BadFormatException("The passed token has a wrong format.");
    token = tokenArray[1];
    log.info("Retrieving all vim instances");
    Iterable<BaseVimInstance> vimInstances = vimManagement.queryByProjectId(projectId);
    if (!componentManager.isService(token))
      for (BaseVimInstance vim : vimInstances) {
        handlePrivateInfo(vim);
      }
    return (List<BaseVimInstance>) vimInstances;
  }

  /**
   * Returns the Datacenter selected by id
   *
   * @param id: The Datacenter's id selected
   * @return Datacenter: The Datacenter selected
   */
  @ApiOperation(
      value = "Retrieve a Vim Instance",
      notes = "Returns the Vim Instance JSON belonging to the id specified in the URL")
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public BaseVimInstance findById(
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId,
      @RequestHeader(value = "authorization") String token)
      throws NotFoundException, BadFormatException {
    log.info("Retrieving vim instance id: " + id);
    BaseVimInstance vim = vimManagement.query(id, projectId);
    if (vim == null) throw new NotFoundException("VIM Instance with ID " + id + " not found.");
    String[] tokenArray = token.split(" ");
    if (tokenArray.length < 2) throw new BadFormatException("The passed token has a wrong format.");
    token = tokenArray[1];
    if (!componentManager.isService(token)) {
      handlePrivateInfo(vim);
    }
    return vim;
  }

  /**
   * Returns the Datacenter selected by name
   *
   * @param name: The Datacenter's name
   * @return Datacenter: The Datacenter requested
   */
  @ApiOperation(
      value = "Retrieve a Vim Instance by name",
      notes = "Returns the Vim Instance JSON belonging to the name specified")
  @GetMapping(value = "search/findByName")
  public BaseVimInstance findByName(
      @RequestParam String name, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return vimManagement.queryByProjectIdAndName(projectId, name);
  }

  /**
   * This operation updates the Network Service Descriptor (NSD)
   *
   * @param newVimInstance the new datacenter to be updated to
   * @param id the id of the old datacenter
   * @return VimInstance the VimInstance updated
   */
  @ApiOperation(
      value = "Updating a Vim Instance",
      notes =
          "Takes a Vim Instance as JSON and updates the Vim Instance with the id specified in the URL")
  @RequestMapping(
      value = "{id}",
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public BaseVimInstance update(
      @RequestBody @Valid BaseVimInstance newVimInstance,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, IOException, BadRequestException, NotFoundException,
          ExecutionException, InterruptedException {
    log.info("Updating vim instance with id: " + id);
    log.debug("New vim instance: " + newVimInstance);
    return vimManagement.update(newVimInstance, id, projectId).get();
  }

  /**
   * Returns the list of NFVImage into a VimInstance with id
   *
   * @param id of the VimInstance
   * @return Set<NFVImage>
   */
  @ApiOperation(
      value = "Retrieve the data about images on a Vim Instance",
      notes =
          "Retrieve the data for all images on the Vim Instance which id is specified in the URL")
  @RequestMapping(value = "{id}/images", method = RequestMethod.GET)
  public Set<? extends BaseNfvImage> getAllImages(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    log.info("Retrieving images from vim instance id: " + id);
    BaseVimInstance vimInstance = vimManagement.query(id, projectId);
    if (vimInstance == null)
      throw new NotFoundException("VIM Instance with ID " + id + " not found.");
    return vimInstance.getImages();
  }

  /**
   * Returns the {@code NFVImage} selected by idImage from {@code VimInstance} with idVim
   *
   * @param idVim of {@code VimInstance}
   * @param idImage of {@code NFVImage}
   * @return {@code NFVImage} selected
   */
  @ApiOperation(
      value = "Returns the data about an Image from the Vim Instance",
      notes =
          "Retrieve the data for a specific image on the Vim Instance which id is specified in the URL")
  @RequestMapping(value = "{idVim}/images/{idImage}", method = RequestMethod.GET)
  public BaseNfvImage getImage(
      @PathVariable("idVim") String idVim,
      @PathVariable("idImage") String idImage,
      @RequestHeader(value = "project-id") String projectId)
      throws EntityUnreachableException, NotFoundException {
    log.info("Retrieving image " + idImage + " from vim instance id: " + idVim);
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
  @ApiOperation(
      value = "Adds an image to the Vim Instance",
      notes = "Adds an image to the Vim Instance with id specified in the URL")
  @RequestMapping(value = "{id}/images", method = RequestMethod.POST)
  public BaseNfvImage addImage(
      @PathVariable("id") String id,
      BaseNfvImage nfvImage,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          NotFoundException {
    log.info("Adding image to vim instance id: " + id);
    log.debug("Image to add: " + nfvImage);
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
  @ApiOperation(
      value = "Updates the data of an image on the Vim Instance",
      notes = "Updates an image to the Vim Instance with id specified in the URL")
  @RequestMapping(value = "{idVim}/images/{idImage}", method = RequestMethod.PUT)
  public BaseNfvImage updateImage(
      @PathVariable("idVim") String idVim,
      @RequestBody @Valid BaseNfvImage image,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          NotFoundException {
    return vimManagement.addImage(idVim, image, projectId);
  }

  /**
   * Removes the {@code NFVImage} with idImage from {@code VimInstance} with idVim
   *
   * @param idVim of {@code VimInstance}
   * @param idImage of {@code NFVImage}
   * @throws VimException
   */
  @ApiOperation(
      value = "Remove an image on the Vim Instance",
      notes = "Remove the specified by id image from the vim instance")
  @RequestMapping(value = "{idVim}/images/{idImage}", method = RequestMethod.DELETE)
  public void deleteImage(
      @PathVariable("idVim") String idVim,
      @PathVariable("idImage") String idImage,
      @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, EntityUnreachableException, IOException,
          NotFoundException {
    log.info("Deleting image " + idImage + " from vim instance id: " + idVim);
    vimManagement.deleteImage(idVim, idImage, projectId);
  }

  /**
   * Returns the refreshed Datacenter selected by id
   *
   * @param id: The Datacenter's id selected
   * @return Datacenter: The Datacenter selected
   */
  @ApiOperation(
      value = "Refreshes the data about the Vim Instance",
      notes = "Refreshes the data about the Vim Instance and returns it")
  @RequestMapping(value = "{id}/refresh", method = RequestMethod.GET)
  public BaseVimInstance refresh(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws VimException, PluginException, IOException, NotFoundException, ExecutionException,
          InterruptedException {
    BaseVimInstance vimInstance = vimManagement.query(id, projectId);
    if (vimInstance == null)
      throw new NotFoundException("VIM Instance with ID " + id + " not found.");
    log.info("Refreshing vim instance: " + id);
    vimManagement.refresh(vimInstance, true).get();
    return vimInstance;
  }
}
