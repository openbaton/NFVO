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

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.*;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.api.exceptions.StateException;
import org.openbaton.nfvo.api.model.DependencyObject;
import org.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/ns-records")
public class RestNetworkServiceRecord {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NetworkServiceRecordManagement networkServiceRecordManagement;

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   *
   * @param networkServiceDescriptor : the Network Service Descriptor to be created
   * @return NetworkServiceRecord: the Network Service Descriptor filled with id and values from
   * core
   */
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public NetworkServiceRecord create(
      @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
      @RequestHeader(value = "project-id") String projectId)
      throws InterruptedException, ExecutionException, VimException, NotFoundException,
          BadFormatException, VimDriverException, QuotaExceededException, PluginException {
    return networkServiceRecordManagement.onboard(networkServiceDescriptor, projectId);
  }

  @RequestMapping(
    value = "{id}",
    method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public NetworkServiceRecord create(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws InterruptedException, ExecutionException, VimException, NotFoundException,
          BadFormatException, VimDriverException, QuotaExceededException, PluginException {
    return networkServiceRecordManagement.onboard(id, projectId);
  }

  /**
   * This operation is used to remove a disabled Network Service Descriptor
   *
   * @param id : the id of Network Service Descriptor
   */
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws VimException, InterruptedException, ExecutionException, NotFoundException {
    try {
      networkServiceRecordManagement.delete(id, projectId);
    } catch (WrongStatusException e) {
      e.printStackTrace();
      throw new StateException(id);
    }
  }

  /**
   * Removes multiple Network Service Descriptor from the NSDescriptors repository
   *
   * @param ids: the id list of Network Service Descriptors
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
    for (String id : ids) networkServiceRecordManagement.delete(id, projectId);
  }

  /**
   * This operation returns the list of Network Service Descriptor (NSD)
   *
   * @return List<NetworkServiceRecord>: the list of Network Service Descriptor stored
   */
  @RequestMapping(method = RequestMethod.GET)
  public List<NetworkServiceRecord> findAll(@RequestHeader(value = "project-id") String projectId) {
    return networkServiceRecordManagement.queryByProjectId(projectId);
  }

  /**
   * This operation returns the Network Service Descriptor (NSD) selected by id
   *
   * @param id : the id of Network Service Descriptor
   * @return NetworkServiceRecord: the Network Service Descriptor selected
   */
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public NetworkServiceRecord findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    return networkServiceRecordManagement.query(id, projectId);
  }

  /**
   * This operation updates the Network Service Descriptor (NSD)
   *
   * @param networkServiceRecord : the Network Service Descriptor to be updated
   * @param id : the id of Network Service Descriptor
   * @return NetworkServiceRecord: the Network Service Descriptor updated
   */
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NetworkServiceRecord update(
      @RequestBody @Valid NetworkServiceRecord networkServiceRecord,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return networkServiceRecordManagement.update(networkServiceRecord, id, projectId);
  }

  /**
   * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
   *
   * @param id of NSD
   * @return Set<VirtualNetworkFunctionDescriptor>: List of VirtualNetworkFunctionDescriptor into
   * NSD
   */
  @RequestMapping(
    value = "{id}/vnfrecords",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<VirtualNetworkFunctionRecord> getVirtualNetworkFunctionRecords(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    log.trace("*****" + nsr.getVnfr().toString());
    return nsr.getVnfr();
  }

  /**
   * Returns the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
   *
   * @param idNsr of NSR
   * @param idVnf of VirtualNetworkFunctionRecord
   * @return VirtualNetworkFunctionRecord selected by idVnf
   * @throws NotFoundException
   */
  @RequestMapping(
    value = "{idNsr}/vnfrecords/{idVnf}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
      @PathVariable("idNsr") String idNsr,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {

    return networkServiceRecordManagement.getVirtualNetworkFunctionRecord(idNsr, idVnf, projectId);
  }

  /**
   * Removes the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
   *
   * @param idNsr
   * @param idVnf
   * @throws NotFoundException
   */
  @RequestMapping(value = "{idNsr}/vnfrecords/{idVnf}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFRecord(
      @PathVariable("idNsr") String idNsr,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceRecordManagement.deleteVNFRecord(idNsr, idVnf, projectId);
  }

  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public void postVNFCInstance(
      @RequestBody @Valid VNFComponent component,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.trace("Received: " + component);
    networkServiceRecordManagement.addVNFCInstance(id, idVnf, idVdu, component, "", projectId);
  }

  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/vnfcinstances",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public void postVNFCInstance(
      @RequestBody @Valid VNFComponent component,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.trace("Received: " + component);
    networkServiceRecordManagement.addVNFCInstance(id, idVnf, component, projectId);
  }

  /////// Fault management utilities
  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances/standby",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public void postStandByVNFCInstance(
      @RequestBody @Valid VNFComponent component,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.debug("PostStandByVNFCInstance received the component: " + component);
    networkServiceRecordManagement.addVNFCInstance(
        id, idVnf, idVdu, component, "standby", projectId);
  }

  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances/{idVNFC}/switchtostandby",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public void switchToRendundantVNFCInstance(
      @RequestBody @Valid VNFCInstance failedVnfcInstance,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @PathVariable("idVNFC") String idVNFC,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.debug("switch to a standby component");
    networkServiceRecordManagement.switchToRedundantVNFCInstance(
        id, idVnf, idVdu, idVNFC, "standby", failedVnfcInstance, projectId);
  }

  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances/{idVNFCI}",
    method = RequestMethod.DELETE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFCInstance(
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @PathVariable("idVNFCI") String idVNFCI,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException, InterruptedException,
          ExecutionException, VimException, PluginException {
    networkServiceRecordManagement.deleteVNFCInstance(id, idVnf, idVdu, idVNFCI, projectId);
  }

  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/vnfcinstances",
    method = RequestMethod.DELETE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFCInstance(
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException, InterruptedException,
          ExecutionException, VimException, PluginException {
    networkServiceRecordManagement.deleteVNFCInstance(id, idVnf, projectId);
  }

  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances",
    method = RequestMethod.DELETE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFCInstance(
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException, InterruptedException,
          ExecutionException, VimException, PluginException {
    networkServiceRecordManagement.deleteVNFCInstance(id, idVnf, idVdu, null, projectId);
  }

  // Rest method for execute actions at different level
  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances/{idVNFCI}/actions",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public void postAction(
      @RequestBody @Valid NFVMessage nfvMessage,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @PathVariable("idVNFCI") String idVNFCI,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    log.debug("Received: " + nfvMessage);
    networkServiceRecordManagement.executeAction(nfvMessage, id, idVnf, idVdu, idVNFCI, projectId);
  }

  @RequestMapping(
    value = "{id}/vnfrecords/",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public VirtualNetworkFunctionRecord postVNFR(
      @RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsd = networkServiceRecordManagement.query(id, projectId);
    nsd.getVnfr().add(vnfRecord);
    networkServiceRecordManagement.update(nsd, id, projectId);
    return vnfRecord;
  }

  @RequestMapping(
    value = "{idNsr}/vnfrecords/{idVnf}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VirtualNetworkFunctionRecord updateVNF(
      @RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
      @PathVariable("idNsr") String idNsr,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsd = networkServiceRecordManagement.query(idNsr, projectId);
    nsd.getVnfr().add(vnfRecord);
    networkServiceRecordManagement.update(nsd, idNsr, projectId);
    return vnfRecord;
  }

  /**
   * Returns the list of VNFDependency into a NSD with id
   *
   * @param id : The id of NSD
   * @return List<VNFDependency>: The List of VNFDependency into NSD
   */
  @RequestMapping(
    value = "{id}/vnfdependencies",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<VNFRecordDependency> getVNFDependencies(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceRecord nsd = networkServiceRecordManagement.query(id, projectId);
    return nsd.getVnf_dependency();
  }

  @RequestMapping(
    value = "{id}/vnfdependenciesList",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<DependencyObject> getVNFDependenciesList(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    Set<DependencyObject> result = new HashSet<>();
    for (VNFRecordDependency vnfDependency : nsr.getVnf_dependency()) {
      for (Entry<String, VNFCDependencyParameters> entry :
          vnfDependency.getVnfcParameters().entrySet()) {
        for (Entry<String, DependencyParameters> parameters :
            entry.getValue().getParameters().entrySet()) {
          DependencyObject dependencyObject = new DependencyObject();
          dependencyObject.setTarget(vnfDependency.getTarget());
          String source = getVNFCHostname(nsr, parameters.getKey());
          dependencyObject.setSource(source);
          result.add(dependencyObject);
        }
      }
    }
    return result;
  }

  private String getVNFCHostname(NetworkServiceRecord nsr, String vnfcId) {
    for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
      for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
        for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
          if (vnfcInstance.getId().equals(vnfcId)) return vnfcInstance.getHostname();
        }
      }
    }
    return null;
  }

  @RequestMapping(
    value = "{id}/vnfdependencies/{id_vnfr}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public VNFRecordDependency getVNFDependency(
      @PathVariable("id") String id,
      @PathVariable("id_vnfr") String id_vnfr,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    return findVNFD(nsr.getVnf_dependency(), id_vnfr);
  }

  @RequestMapping(value = "{idNsr}/vnfdependencies/{idVnfd}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFDependency(
      @PathVariable("idNsr") String idNsr,
      @PathVariable("idVnfd") String idVnfd,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceRecordManagement.deleteVNFDependency(idNsr, idVnfd, projectId);
  }

  @RequestMapping(
    value = "{id}/vnfdependencies/",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public VNFRecordDependency postVNFDependency(
      @RequestBody @Valid VNFRecordDependency vnfDependency,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    nsr.getVnf_dependency().add(vnfDependency);
    networkServiceRecordManagement.update(nsr, id, projectId);
    return vnfDependency;
  }

  @RequestMapping(
    value = "{id}/vnfdependencies/{id_vnfd}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public VNFRecordDependency updateVNFD(
      @RequestBody @Valid VNFRecordDependency vnfDependency,
      @PathVariable("id") String id,
      @PathVariable("id_vnfd") String id_vnfd,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    nsr.getVnf_dependency().add(vnfDependency);
    networkServiceRecordManagement.update(nsr, id, projectId);
    return vnfDependency;
  }

  /**
   * Returns the list of PhysicalNetworkFunctionRecord into a NSD with id
   *
   * @param id : The id of NSD
   * @return List<PhysicalNetworkFunctionRecord>: The List of PhysicalNetworkFunctionRecord into NSD
   */
  @RequestMapping(
    value = "{id}/pnfrecords",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<PhysicalNetworkFunctionRecord> getPhysicalNetworkFunctionRecord(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId) {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    return nsr.getPnfr();
  }

  /**
   * Returns the PhysicalNetworkFunctionRecord
   *
   * @param id : The NSD id
   * @param id_pnf : The PhysicalNetworkFunctionRecord id
   * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord selected
   */
  @RequestMapping(
    value = "{id}/pnfrecords/{id_pnf}",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public PhysicalNetworkFunctionRecord getPhysicalNetworkFunctionRecord(
      @PathVariable("id") String id,
      @PathVariable("id_pnf") String id_pnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsd = networkServiceRecordManagement.query(id, projectId);
    return findPNFD(nsd.getPnfr(), id_pnf);
  }

  /**
   * Deletes the PhysicalNetworkFunctionRecord with the id_pnf
   *
   * @param id : The NSD id
   * @param id_pnf : The PhysicalNetworkFunctionRecord id
   */
  @RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePhysicalNetworkFunctionRecord(
      @PathVariable("id") String id,
      @PathVariable("id_pnf") String id_pnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    PhysicalNetworkFunctionRecord pDescriptor = findPNFD(nsr.getPnfr(), id_pnf);
    nsr.getVnfr().remove(pDescriptor);
  }

  /**
   * Stores the PhysicalNetworkFunctionRecord
   *
   * @param pDescriptor : The PhysicalNetworkFunctionRecord to be stored
   * @param id : The NSD id
   * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord stored
   */
  @RequestMapping(
    value = "{id}/pnfrecords/",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public PhysicalNetworkFunctionRecord postPhysicalNetworkFunctionRecord(
      @RequestBody @Valid PhysicalNetworkFunctionRecord pDescriptor,
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsd = networkServiceRecordManagement.query(id, projectId);
    nsd.getPnfr().add(pDescriptor);
    networkServiceRecordManagement.update(nsd, id, projectId);
    return pDescriptor;
  }

  /**
   * Edits the PhysicalNetworkFunctionRecord
   *
   * @param pRecord : The PhysicalNetworkFunctionRecord to be edited
   * @param id : The NSD id
   * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord edited
   */
  @RequestMapping(
    value = "{id}/pnfrecords/{id_pnf}",
    method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public PhysicalNetworkFunctionRecord updatePNFD(
      @RequestBody @Valid PhysicalNetworkFunctionRecord pRecord,
      @PathVariable("id") String id,
      @PathVariable("id_pnf") String id_pnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsd = networkServiceRecordManagement.query(id, projectId);
    nsd.getPnfr().add(pRecord);
    networkServiceRecordManagement.update(nsd, id, projectId);
    return pRecord;
  }

  // TODO The Rest of the classes

  private PhysicalNetworkFunctionRecord findPNFD(
      Collection<PhysicalNetworkFunctionRecord> listPNFR, String id_pnf) throws NotFoundException {
    for (PhysicalNetworkFunctionRecord pRecord : listPNFR) {
      if (pRecord.getId().equals(id_pnf)) {
        return pRecord;
      }
    }
    throw new NotFoundException("PNFD with id " + id_pnf + " was not found");
  }

  private VNFRecordDependency findVNFD(
      Collection<VNFRecordDependency> vnf_dependency, String id_vnfd) throws NotFoundException {
    for (VNFRecordDependency vnfDependency : vnf_dependency) {
      if (vnfDependency.getId().equals(id_vnfd)) {
        return vnfDependency;
      }
    }
    throw new NotFoundException("VNFD with id " + id_vnfd + " was not found");
  }

  private VirtualNetworkFunctionRecord findVNF(
      Collection<VirtualNetworkFunctionRecord> listVNF, String id_vnf) throws NotFoundException {
    for (VirtualNetworkFunctionRecord vnfRecord : listVNF) {
      if (vnfRecord.getId().equals(id_vnf)) {
        return vnfRecord;
      }
    }
    throw new NotFoundException("VNFR with id " + id_vnf + " was not found");
  }
}
