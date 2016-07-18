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
import org.openbaton.nfvo.core.interfaces.VirtualNetworkFunctionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vnf-descriptors")
public class RestVirtualNetworkFunctionDescriptor {

  //	TODO add log prints
  //	private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private VirtualNetworkFunctionManagement vnfdManagement;

  /**
   * Adds a new VNF software Image to the image repository
   *
   * @param virtualNetworkFunctionDescriptor : VirtualNetworkFunctionDescriptor to add
   * @return VirtualNetworkFunctionDescriptor: The VirtualNetworkFunctionDescriptor filled with
   * values from the core
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public VirtualNetworkFunctionDescriptor create(
      @RequestBody @Valid VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      @RequestHeader(value = "project-id") String projectId) {
    return vnfdManagement.add(virtualNetworkFunctionDescriptor, projectId);
  }

  /**
   * Removes the VNF software virtualNetworkFunctionDescriptor from the
   * virtualNetworkFunctionDescriptor repository
   *
   * @param id : The virtualNetworkFunctionDescriptor's id to be deleted
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {

    vnfdManagement.delete(id, projectId);
  }

  /**
   * Removes multiple VirtualNetworkFunctionDescriptor from the VirtualNetworkFunctionDescriptors
   * repository
   *
   * @param ids
   */
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId) {
    for (String id : ids) vnfdManagement.delete(id, projectId);
  }

  /**
   * Returns the list of the VNF software virtualNetworkFunctionDescriptors available
   *
   * @return List<virtualNetworkFunctionDescriptor>: The list of VNF software
   * virtualNetworkFunctionDescriptors available
   */
  @RequestMapping(method = RequestMethod.GET)
  public Iterable<VirtualNetworkFunctionDescriptor> findAll(
      @RequestHeader(value = "project-id") String projectId) {
    return vnfdManagement.queryByProjectId(projectId);
  }

  /**
   * Returns the VNF software virtualNetworkFunctionDescriptor selected by id
   *
   * @param id : The id of the VNF software virtualNetworkFunctionDescriptor
   * @return virtualNetworkFunctionDescriptor: The VNF software virtualNetworkFunctionDescriptor
   * selected
   */
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public VirtualNetworkFunctionDescriptor findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {

    return vnfdManagement.query(id, projectId);
  }

  /**
   * Updates the VNF software virtualNetworkFunctionDescriptor
   *
   * @param virtualNetworkFunctionDescriptor : the VNF software virtualNetworkFunctionDescriptor to
   * be updated
   * @param id : the id of VNF software virtualNetworkFunctionDescriptor
   * @return networkServiceDescriptor: the VNF software virtualNetworkFunctionDescriptor updated
   */
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VirtualNetworkFunctionDescriptor update(
      @RequestBody @Valid VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId) {
    return vnfdManagement.update(virtualNetworkFunctionDescriptor, id, projectId);
  }
}
