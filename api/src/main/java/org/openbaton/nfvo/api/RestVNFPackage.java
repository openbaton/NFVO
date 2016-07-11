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

package org.openbaton.nfvo.api;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongAction;
import org.openbaton.nfvo.core.interfaces.VNFPackageManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vnf-packages")
public class RestVNFPackage {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VNFPackageManagement vnfPackageManagement;

  /**
   * Adds a new VNFPackage to the VNFPackages repository
   */
  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public String onboard(
      @RequestParam("file") MultipartFile file,
      @RequestHeader(value = "project-id") String projectId)
      throws IOException, VimException, NotFoundException, SQLException, PluginException {

    log.debug("Onboarding");
    if (!file.isEmpty()) {
      byte[] bytes = file.getBytes();
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
          vnfPackageManagement.onboard(bytes, projectId);
      return "{ \"id\": \"" + virtualNetworkFunctionDescriptor.getVnfPackageLocation() + "\"}";
    } else throw new IOException("File is empty!");
  }

  /**
   * Removes the VNFPackage from the VNFPackages repository
   *
   * @param id : the id of configuration to be removed
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws WrongAction {
    vnfPackageManagement.delete(id, projectId);
  }
  /**
   * Removes multiple VNFPackage from the VNFPackages repository
   *
   * @param ids: The List of the VNFPackage Id to be deleted
   * @throws NotFoundException, WrongAction
   */
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, WrongAction {
    for (String id : ids) vnfPackageManagement.delete(id, projectId);
  }

  /**
   * Returns the list of the VNFPackages available
   *
   * @return List<VNFPackage>: The list of VNFPackages available
   */
  @RequestMapping(method = RequestMethod.GET)
  public Iterable<VNFPackage> findAll(@RequestHeader(value = "project-id") String projectId) {
    return vnfPackageManagement.queryByProjectId(projectId);
  }

  @RequestMapping(
    value = "{id}/scripts/{scriptId}",
    method = RequestMethod.GET,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String getScript(
      @PathVariable("id") String id,
      @PathVariable("scriptId") String scriptId,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    VNFPackage vnfPackage = vnfPackageManagement.query(id, projectId);
    for (Script script : vnfPackage.getScripts()) {
      if (script.getId().equals(scriptId)) {
        return new String(script.getPayload());
      }
    }
    throw new NotFoundException(
        "Script with id " + scriptId + " was not found into package with id " + id);
  }

  @RequestMapping(
    value = "{id}/scripts/{scriptId}",
    method = RequestMethod.PUT,
    produces = MediaType.TEXT_PLAIN_VALUE,
    consumes = MediaType.TEXT_PLAIN_VALUE
  )
  public String updateScript(
      @PathVariable("id") String id,
      @PathVariable("scriptId") String scriptId,
      @RequestBody String scriptNew,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    VNFPackage vnfPackage = vnfPackageManagement.query(id, projectId);
    for (Script script : vnfPackage.getScripts()) {
      if (script.getId().equals(scriptId)) {
        script.setPayload(scriptNew.getBytes());
        script = vnfPackageManagement.updateScript(script);
        return new String(script.getPayload());
      }
    }
    throw new NotFoundException(
        "Script with id " + scriptId + " was not found into package with id " + id);
  }

  /**
   * Returns the VNFPackage selected by id
   *
   * @param id : The id of the VNFPackage
   * @return VNFPackage: The VNFPackage selected
   */
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public VNFPackage findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    return vnfPackageManagement.query(id, projectId);
  }

  /**
   * Updates the VNFPackage
   *
   * @param vnfPackage_new : The VNFPackage to be updated
   * @param id : The id of the VNFPackage
   * @return VNFPackage The VNFPackage updated
   */
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VNFPackage update(
      @RequestBody @Valid VNFPackage vnfPackage_new,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId) {
    return vnfPackageManagement.update(id, vnfPackage_new, projectId);
  }
}
