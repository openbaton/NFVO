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

import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;

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
   * core
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public NetworkServiceDescriptor create(
      @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, NetworkServiceIntegrityException,
          CyclicDependenciesException {
    NetworkServiceDescriptor nsd;
    log.trace("Just Received: " + networkServiceDescriptor);
    nsd = networkServiceDescriptorManagement.onboard(networkServiceDescriptor, projectId);
    return nsd;
  }

  /**
   * This operation is used to remove a disabled Network Service Descriptor
   *
   * @param id of Network Service Descriptor
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws WrongStatusException {
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws InterruptedException, ExecutionException, WrongStatusException, VimException,
          NotFoundException {
    for (String id : ids) networkServiceDescriptorManagement.delete(id, projectId);
  }

  /**
   * This operation returns the list of Network Service Descriptor (NSD)
   *
   * @return List<NetworkServiceDescriptor>: the list of Network Service Descriptor stored
   */
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
      @RequestHeader(value = "project-id") String projectId) {
    return networkServiceDescriptorManagement.update(networkServiceDescriptor, projectId);
  }

  /**
   * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
   *
   * @param id : The id of NSD
   * @return List<VirtualNetworkFunctionDescriptor>: The List of VirtualNetworkFunctionDescriptor
   * into NSD @
   */
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

  @RequestMapping(value = "{idNsd}/vnfdescriptors/{idVfn}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVirtualNetworkFunctionDescriptor(
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVfn") String idVfn,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceDescriptorManagement.deleteVnfDescriptor(idNsd, idVfn, projectId);
  }

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

  @RequestMapping(value = "{idNsd}/vnfdependencies/{idVnfd}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFDependency(
      @PathVariable("idNsd") String idNsd,
      @PathVariable("idVnfd") String idVnfd,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceDescriptorManagement.deleteVNFDependency(idNsd, idVnfd, projectId);
  }

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
   * into NSD @
   */
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
