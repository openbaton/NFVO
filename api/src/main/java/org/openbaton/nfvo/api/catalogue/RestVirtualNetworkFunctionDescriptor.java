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

package org.openbaton.nfvo.api.catalogue;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.VirtualNetworkFunctionManagement;
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
   *     values from the core
   */
  @ApiOperation(
    value = "Adding a Virtual Network Function Descriptor",
    notes =
        "POST request with Virtual Network Function Descriptor as JSON content of the request body"
  )
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public VirtualNetworkFunctionDescriptor create(
      @RequestBody @Valid VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, NetworkServiceIntegrityException {
    return vnfdManagement.add(virtualNetworkFunctionDescriptor, projectId);
  }

  /**
   * Removes the VNF software virtualNetworkFunctionDescriptor from the
   * virtualNetworkFunctionDescriptor repository
   *
   * @param id : The virtualNetworkFunctionDescriptor's id to be deleted
   */
  @ApiOperation(
    value = "Removing a Virtual Network Function Descriptor",
    notes =
        "The id in the URL belongs to the Virtual Network Function Descriptor that will be deleted"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws EntityInUseException, NotFoundException {

    vnfdManagement.delete(id, projectId);
  }

  /**
   * Removes multiple VirtualNetworkFunctionDescriptor from the VirtualNetworkFunctionDescriptors
   * repository
   *
   * @param ids
   */
  @ApiOperation(
    value = "Removing multiple Virtual Network Function Descriptors",
    notes =
        "The request takes a list of ids of Virtual Network Function Descriptor that will be deleted"
  )
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws EntityInUseException, NotFoundException {
    for (String id : ids) vnfdManagement.delete(id, projectId);
  }

  /**
   * Returns the list of the VNF software virtualNetworkFunctionDescriptors available
   *
   * @return List<virtualNetworkFunctionDescriptor>: The list of VNF software
   *     virtualNetworkFunctionDescriptors available
   */
  @ApiOperation(value = "Retrieving all Virtual Network Function Descriptors", notes = "")
  @RequestMapping(method = RequestMethod.GET)
  public List<VirtualNetworkFunctionDescriptor> findAll(
      @RequestHeader(value = "project-id") String projectId) {
    return (List<VirtualNetworkFunctionDescriptor>) vnfdManagement.queryByProjectId(projectId);
  }

  /**
   * Returns the VNF software virtualNetworkFunctionDescriptor selected by id
   *
   * @param id : The id of the VNF software virtualNetworkFunctionDescriptor
   * @return virtualNetworkFunctionDescriptor: The VNF software virtualNetworkFunctionDescriptor
   *     selected
   */
  @ApiOperation(
    value = "Retrieving a Virtual Network Function Descriptor",
    notes = "The id in the URL belongs to the requested Virtual Network Function Descriptor"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public VirtualNetworkFunctionDescriptor findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {

    VirtualNetworkFunctionDescriptor vnfd = vnfdManagement.query(id, projectId);
    if (vnfd == null) throw new NotFoundException("No VNFD found with ID " + id);
    return vnfd;
  }

  /**
   * Updates the VNF software virtualNetworkFunctionDescriptor
   *
   * @param virtualNetworkFunctionDescriptor : the VNF software virtualNetworkFunctionDescriptor to
   *     be updated
   * @param id : the id of VNF software virtualNetworkFunctionDescriptor
   * @return networkServiceDescriptor: the VNF software virtualNetworkFunctionDescriptor updated
   */
  @ApiOperation(
    value = "Updating a Virtual Network Function Descriptor",
    notes =
        "The updated VNFD is passed as JSON content in the Request Body and the id in the URL belongs to the VNFD that will be updated"
  )
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
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return vnfdManagement.update(virtualNetworkFunctionDescriptor, id, projectId);
  }

  @ApiOperation(
    value = "Retrieving a Virtual Deployment Unit",
    notes =
        "The vnfdId in the URL belongs to the requested Virtual Network Function Descriptor and the vduId to the Virtual Deployment Unit inside the VNFD"
  )
  @RequestMapping(value = "{vnfdId}/vdus/{vduId}", method = RequestMethod.GET)
  public VirtualDeploymentUnit getVDU(
      @PathVariable("vnfdId") String vnfdId,
      @PathVariable("vduId") String vduId,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    VirtualNetworkFunctionDescriptor vnfd = vnfdManagement.query(vnfdId, projectId);
    if (vnfd == null) throw new NotFoundException("No VNFD found with ID " + vnfdId);
    for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
      if (vdu.getId().equals(vduId)) return vdu;
    }
    throw new NotFoundException(
        "No VDU with ID " + vduId + " was found in the VNFD with ID " + vnfdId);
  }
}
