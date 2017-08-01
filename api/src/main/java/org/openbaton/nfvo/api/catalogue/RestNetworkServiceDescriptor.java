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

import com.google.gson.JsonObject;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.EntityUnreachableException;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongStatusException;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/ns-descriptors")
public class RestNetworkServiceDescriptor {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private NetworkServiceDescriptorManagement networkServiceDescriptorManagement;
  @Autowired private NetworkServiceRecordManagement networkServiceRecordManagement;

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   *
   * @param networkServiceDescriptor : the Network Service Descriptor to be created
   * @return networkServiceDescriptor: the Network Service Descriptor filled with id and values from
   *     core
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(
    value = "Adding a Network Service Descriptor",
    notes = "POST request with Network Service Descriptor as JSON content of the request body"
  )
  public NetworkServiceDescriptor create(
      @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, NetworkServiceIntegrityException,
          CyclicDependenciesException, EntityInUseException, BadRequestException, IOException,
          AlreadyExistingException, PluginException, IncompatibleVNFPackage, VimException,
          InterruptedException, EntityUnreachableException {
    NetworkServiceDescriptor nsd;
    log.trace("Just Received: " + networkServiceDescriptor);
    nsd = networkServiceDescriptorManagement.onboard(networkServiceDescriptor, projectId);
    return nsd;
  }

  /**
   * This operation allows downloading and onboarding an NSD from the Marketplace
   *
   * @param link : link to the Network Service Descriptor to be created
   * @return networkServiceDescriptor: the Network Service Descriptor filled with id and values from
   *     core
   */
  @ApiOperation(
    value = " Adding a NSD from the marketplace",
    notes = "POST request with the a JSON object in the request body containing a field named link"
  )
  @RequestMapping(
    value = "/marketdownload",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public NetworkServiceDescriptor marketDownload(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws BadFormatException, CyclicDependenciesException, NetworkServiceIntegrityException,
          NotFoundException, IOException, PluginException, VimException, IncompatibleVNFPackage,
          AlreadyExistingException, EntityInUseException, BadRequestException, InterruptedException,
          EntityUnreachableException {

    log.debug("LINK: " + link);
    String downloadlink = link.get("link").getAsString();
    return networkServiceDescriptorManagement.onboardFromMarketplace(downloadlink, projectId);
  }

  /**
   * This operation allows downloading and onboarding an NSD from the Package Repository
   *
   * @param link : link to the Network Service Descriptor to be created
   * @return networkServiceDescriptor: the Network Service Descriptor filled with id and values from
   *     core
   */
  @ApiOperation(
    value = " Adding a NSD from the marketplace",
    notes = "POST request with the a JSON object in the request body containing a field named link"
  )
  @RequestMapping(
    value = "/package-repository-download",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public NetworkServiceDescriptor packageRepositoryDownload(
      @RequestBody JsonObject link, @RequestHeader(value = "project-id") String projectId)
      throws BadFormatException, CyclicDependenciesException, NetworkServiceIntegrityException,
          NotFoundException, IOException, PluginException, VimException, IncompatibleVNFPackage,
          AlreadyExistingException, EntityInUseException, BadRequestException,
          EntityUnreachableException, InterruptedException {

    log.debug("Received request to download nsd from Package Repository from this link: " + link);
    String downloadlink = link.get("link").getAsString();
    return networkServiceDescriptorManagement.onboardFromPackageRepository(downloadlink, projectId);
  }

  /**
   * This operation is used to remove a disabled Network Service Descriptor
   *
   * @param id of Network Service Descriptor
   */
  @ApiOperation(
    value = "Removing a Network Service Descriptor",
    notes = "DELETE request where the id in the url belongs to the NSD to delete"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws WrongStatusException, EntityInUseException {
    networkServiceDescriptorManagement.delete(id, projectId);
  }

  /**
   * Removes a list Network Service Descriptor from the NSDs Repository
   *
   * @param ids: the list of the ids
   * @throws NotFoundException
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws WrongStatusException
   * @throws VimException
   */
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ApiOperation(
    value = "Removing multiple Network Service Descriptors",
    notes = "Delete Request takes a list of Network Service Descriptor ids"
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws InterruptedException, ExecutionException, WrongStatusException, VimException,
          NotFoundException, EntityInUseException {
    for (String id : ids) networkServiceDescriptorManagement.delete(id, projectId);
  }

  /**
   * This operation returns the list of Network Service Descriptor (NSD)
   *
   * @return List<NetworkServiceDescriptor>: the list of Network Service Descriptor stored
   */
  @ApiOperation(
    value = "Get all NSDs from a project",
    notes = "Returns all Network Service Descriptors onboarded in the project with the specified id"
  )
  @RequestMapping(method = RequestMethod.GET)
  public Iterable<NetworkServiceDescriptor> findAll(
      @RequestHeader(value = "project-id") String projectId) {
    return networkServiceDescriptorManagement.queryByProjectId(projectId);
  }

  /**
   * This operation returns the Network Service Descriptor (NSD) selected by id
   *
   * @param id of Network Service Descriptor
   * @return NetworkServiceDescriptor: the Network Service Descriptor selected @
   */
  @ApiOperation(
    value = "Get Network Service Descriptor by id",
    notes = "Returns the Network Service Descriptor with the id in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public NetworkServiceDescriptor findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    return networkServiceDescriptorManagement.query(id, projectId);
  }

  /**
   * This operation updates the Network Service Descriptor (NSD)
   *
   * @param networkServiceDescriptor : the Network Service Descriptor to be updated
   * @param id : the id of Network Service Descriptor
   * @return networkServiceDescriptor: the Network Service Descriptor updated
   */
  @ApiOperation(
    value = "Update a Network Service Descriptor",
    notes =
        "Takes a Network Service Descriptor and updates the Descriptor with the id provided in the URL with the Descriptor from the request body"
  )
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NetworkServiceDescriptor update(
      @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadRequestException {
    return networkServiceDescriptorManagement.update(networkServiceDescriptor, projectId);
  }

  /**
   * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
   *
   * @param id : The id of NSD
   * @return List<VirtualNetworkFunctionDescriptor>: The List of VirtualNetworkFunctionDescriptor
   *     into NSD @
   */
  @ApiOperation(
    value = "Gets the list of Virtual Network Functions part of the NSD",
    notes = "Returns the VNFDs from the NSD with the id specified in the URL"
  )
  @RequestMapping(
    value = "{id}/vnfdescriptors",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<VirtualNetworkFunctionDescriptor> getVirtualNetworkFunctionDescriptors(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id, projectId);
    return nsd.getVnfd();
  }

  @ApiOperation(
    value = "Gets a single VNF from the NSD",
    notes =
        "Returns the VNFD with the id specified in the URL and that is part of the specified NSD"
  )
  @RequestMapping(
    value = "{idNsd}/vnfdescriptors/{idVfnd}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public VirtualNetworkFunctionDescriptor getVirtualNetworkFunctionDescriptor(
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVfnd") String idVfnd,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return networkServiceDescriptorManagement.getVirtualNetworkFunctionDescriptor(
        idNsd, idVfnd, projectId);
  }

  @ApiOperation(
    value = "Delete VNFD from the NSD",
    notes = "Takes the ids of the NSD and VNFD and updates the NSD"
  )
  @RequestMapping(value = "{idNsd}/vnfdescriptors/{idVfn}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVirtualNetworkFunctionDescriptor(
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVfn") String idVfn,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, EntityInUseException {
    networkServiceDescriptorManagement.deleteVnfDescriptor(idNsd, idVfn, projectId);
  }

  @ApiOperation(
    value = "Add a VNFD to the NSD",
    notes =
        "Takes a Virtual Network Function Descriptor in the Request Body and adds it to the vnfd list of the Network Service Descriptor with id specified in the URL"
  )
  @RequestMapping(
    value = "{id}/vnfdescriptors/",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public VirtualNetworkFunctionDescriptor postVNFD(
      @RequestBody @Valid VirtualNetworkFunctionDescriptor vnfDescriptor,
      @PathVariable("id") String idNsd,
      @RequestHeader(value = "project-id") String projectId) {
    return networkServiceDescriptorManagement.addVnfd(vnfDescriptor, idNsd, projectId);
  }

  @ApiOperation(
    value = "Update a VNFD in the NSD",
    notes =
        "Takes a Virtual Network Function Descriptor in the Request Body and updates the specified vnfd that is part of the Network Service Descriptor with id specified in the URL"
  )
  @RequestMapping(
    value = "{idNsd}/vnfdescriptors/{idVfn}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VirtualNetworkFunctionDescriptor updateVNF(
      @RequestBody @Valid VirtualNetworkFunctionDescriptor vnfDescriptor,
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVfn") String idVfn,
      @RequestHeader(value = "project-id") String projectId) {
    return networkServiceDescriptorManagement.updateVNF(idNsd, idVfn, vnfDescriptor, projectId);
  }

  /**
   * Returns the list of VNFDependency into a NSD with id
   *
   * @param id : The id of NSD
   * @return List<VNFDependency>: The List of VNFDependency into NSD @
   */
  @ApiOperation(
    value = "Returns the list of VNFDependency from NSD",
    notes = "Returns all the VNF Dependencies specified in the NSD"
  )
  @RequestMapping(
    value = "{id}/vnfdependencies",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<VNFDependency> getVNFDependencies(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id, projectId);
    return nsd.getVnf_dependency();
  }

  @ApiOperation(
    value = "Returns the list of VNF Dependency for a VNF from the NSD",
    notes = "Returns all the VNF Dependencies only for a specific VNF specified in the NSD"
  )
  @RequestMapping(
    value = "{idNsd}/vnfdependencies/{idVnfd}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public VNFDependency getVNFDependency(
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVnfd") String idVnfd,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return networkServiceDescriptorManagement.getVnfDependency(idNsd, idVnfd, projectId);
  }

  @ApiOperation(
    value = "Delete VNF Dependency from a VNF part of the NSD",
    notes = "Delete the VNF Dependency only for a specific VNF specified in the NSD"
  )
  @RequestMapping(value = "{idNsd}/vnfdependencies/{idVnfd}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFDependency(
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVnfd") String idVnfd,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceDescriptorManagement.deleteVNFDependency(idNsd, idVnfd, projectId);
  }

  @ApiOperation(
    value = "Add a VNF Dependency",
    notes = "Adds a new VNF dependency to the Network Service Descriptor"
  )
  @RequestMapping(
    value = "{idNsd}/vnfdependencies/",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public VNFDependency postVNFDependency(
      @RequestBody @Valid VNFDependency vnfDependency,
      @PathVariable("idNsd") String idNsd,
      @RequestHeader(value = "project-id") String projectId) {
    networkServiceDescriptorManagement.saveVNFDependency(idNsd, vnfDependency, projectId);
    return vnfDependency;
  }

  @ApiOperation(
    value = "Update a VNF Dependency",
    notes = "Updates a VNF dependency to the Network Service Descriptor"
  )
  @RequestMapping(
    value = "{idNsd}/vnfdependencies/{idVnf}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VNFDependency updateVNFDependency(
      @RequestBody @Valid VNFDependency vnfDependency,
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId) {
    networkServiceDescriptorManagement.saveVNFDependency(idNsd, vnfDependency, projectId);
    return vnfDependency;
  }

  /**
   * Returns the list of PhysicalNetworkFunctionDescriptor into a NSD with id
   *
   * @param id : The id of NSD
   * @return List<PhysicalNetworkFunctionDescriptor>: The List of PhysicalNetworkFunctionDescriptor
   *     into NSD @
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{id}/pnfdescriptors",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<PhysicalNetworkFunctionDescriptor> getPhysicalNetworkFunctionDescriptors(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id, projectId);
    return nsd.getPnfd();
  }

  /**
   * Returns the PhysicalNetworkFunctionDescriptor
   *
   * @param idNsd : The NSD id
   * @param idPnf : The PhysicalNetworkFunctionDescriptor id
   * @return PhysicalNetworkFunctionDescriptor: The PhysicalNetworkFunctionDescriptor selected @
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{idNds}/pnfdescriptors/{idPnf}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public PhysicalNetworkFunctionDescriptor getPhysicalNetworkFunctionDescriptor(
      @PathVariable("idNds") String idNsd,
      @PathVariable("idPnf") String idPnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return networkServiceDescriptorManagement.getPhysicalNetworkFunctionDescriptor(
        idNsd, idPnf, projectId);
  }

  /**
   * Deletes the PhysicalNetworkFunctionDescriptor with the idPnf
   *
   * @param idNsd id of NSD
   * @param idPnf id of PhysicalNetworkFunctionDescriptor
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(value = "{idNsd}/pnfdescriptors/{idPnf}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePhysicalNetworkFunctionDescriptor(
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idPnf") String idPnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceDescriptorManagement.deletePhysicalNetworkFunctionDescriptor(
        idNsd, idPnf, projectId);
  }

  /**
   * Stores the PhysicalNetworkFunctionDescriptor
   *
   * @param pDescriptor : The PhysicalNetworkFunctionDescriptor to be stored
   * @param idNsd : The NSD id
   * @return PhysicalNetworkFunctionDescriptor: The PhysicalNetworkFunctionDescriptor stored @
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{id}/pnfdescriptors/",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public PhysicalNetworkFunctionDescriptor postPhysicalNetworkFunctionDescriptor(
      @RequestBody @Valid PhysicalNetworkFunctionDescriptor pDescriptor,
      @PathVariable("id") String idNsd,
      @RequestHeader(value = "project-id") String projectId) {
    return networkServiceDescriptorManagement.addPnfDescriptor(pDescriptor, idNsd, projectId);
  }

  /**
   * Edits the PhysicalNetworkFunctionDescriptor
   *
   * @param pDescriptor : The PhysicalNetworkFunctionDescriptor to be edited
   * @param id : The NSD id
   * @return PhysicalNetworkFunctionDescriptor: The PhysicalNetworkFunctionDescriptor edited @
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{id}/pnfdescriptors/{idPnf}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public PhysicalNetworkFunctionDescriptor updatePNFD(
      @RequestBody @Valid PhysicalNetworkFunctionDescriptor pDescriptor,
      @PathVariable("id") String id,
      @PathVariable("idPnf") String idPnf,
      @RequestHeader(value = "project-id") String projectId) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /**
   * Returns the Security into a NSD with id
   *
   * @param id : The id of NSD
   * @return Security: The Security of PhysicalNetworkFunctionDescriptor into NSD @
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{id}/security",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Security getSecurity(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceDescriptor nsd = networkServiceDescriptorManagement.query(id, projectId);
    return nsd.getNsd_security();
  }

  /**
   * Deletes the Security with the id_s
   *
   * @param idNSD : The NSD id
   * @param idS : The Security id @
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(value = "{id}/security/{idS}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSecurity(
      @PathVariable("id") String idNSD,
      @PathVariable("idS") String idS,
      @RequestHeader(value = "project-id") String projectId) {
    networkServiceDescriptorManagement.deleteSecurty(idNSD, idS, projectId);
  }

  /**
   * Stores the Security into NSD
   *
   * @param security : The Security to be stored
   * @param id : The id of NSD
   * @return Security: The Security stored @
   */
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{id}/security/",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public Security postSecurity(
      @RequestBody @Valid Security security,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId) {
    return networkServiceDescriptorManagement.addSecurity(id, security, projectId);
  }

  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{id}/security/{id_s}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Security updateSecurity(
      @RequestBody @Valid Security security,
      @PathVariable("id") String id,
      @PathVariable("id_s") String id_s,
      @RequestHeader(value = "project-id") String projectId) {
    throw new UnsupportedOperationException();
  }

  //  @RequestMapping(
  //    value = "/records",
  //    method = RequestMethod.POST,
  //    consumes = MediaType.APPLICATION_JSON_VALUE,
  //    produces = MediaType.APPLICATION_JSON_VALUE
  //  )
  //  @ResponseStatus(HttpStatus.CREATED)
  //  public NetworkServiceRecord createRecord(
  //      @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
  //      @RequestHeader(value = "project-id") String projectId)
  //      throws BadFormatException, InterruptedException, ExecutionException, VimException,
  //          NotFoundException, VimDriverException, QuotaExceededException, PluginException {
  //    return networkServiceRecordManagement.onboard(networkServiceDescriptor, projectId, body);
  //  }
}
