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

package org.openbaton.nfvo.api.runtime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.PhysicalNetworkFunctionRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.catalogue.nfvo.HistoryLifecycleEvent;
import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.QuotaExceededException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongStatusException;
import org.openbaton.nfvo.api.model.DependencyObject;
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
@RequestMapping("/api/v1/ns-records")
public class RestNetworkServiceRecord {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private NetworkServiceRecordManagement networkServiceRecordManagement;
  @Autowired private Gson gson;

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   *
   * @param networkServiceDescriptor : the Network Service Descriptor to be created
   * @return NetworkServiceRecord: the Network Service Descriptor filled with id and values from
   *     core
   */
  @ApiOperation(
    value = "Deploying a Network Service Record from a JSON NSD",
    notes =
        "The NSD is passed in the Request Body as a json and the other needed parameters are passed as json in the bodyJson object"
  )
  @RequestMapping(
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  @Deprecated
  public NetworkServiceRecord create(
      @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor,
      @RequestHeader(value = "project-id") String projectId,
      @RequestBody(required = false) String bodyJson)
      throws InterruptedException, ExecutionException, VimException, NotFoundException,
          BadFormatException, PluginException, MissingParameterException, BadRequestException,
          IOException, AlreadyExistingException {

    JsonObject jsonObject;
    try {
      jsonObject = gson.fromJson(bodyJson, JsonObject.class);
    } catch (Exception e) {
      log.error("Exception while parsing Json body.");
      e.printStackTrace();
      throw new BadRequestException("The request's Json body could not be parsed.");
    }
    String monitoringIp = null;
    if (jsonObject.has("monitoringIp")) {
      monitoringIp = jsonObject.get("monitoringIp").getAsString();
    }

    return networkServiceRecordManagement.onboard(
        networkServiceDescriptor,
        projectId,
        gson.fromJson(jsonObject.getAsJsonArray("keys"), List.class),
        gson.fromJson(jsonObject.getAsJsonObject("vduVimInstances"), Map.class),
        gson.fromJson(jsonObject.getAsJsonObject("configurations"), Map.class),
        monitoringIp);
  }

  /**
   * @param id of the NSR
   * @param projectId if of the project
   * @param body the body json is: { "vduVimInstances":{ "vduName1":[ "viminstancename" ],
   *     "vduName2":[ "viminstancename2" ] }, "keys":[ "keyname1", "keyname2" ], "configurations":{
   *     "vnfrName1":{ "name":"conf1", "configurationParameters":[ { "confKey":"key1",
   *     "value":"value1", "description":"description1" }, { "confKey":"key2", "value":"value2",
   *     "description":"description2" } ] }, "vnfrName2":{ "name":"conf1",
   *     "configurationParameters":[ { "confKey":"key1", "value":"value1",
   *     "description":"description1" }, { "confKey":"key2", "value":"value2",
   *     "description":"description2" } ] } }, "monitoringIp" : "192.168.0.1" }
   * @return the created NSR
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws VimException
   * @throws NotFoundException
   * @throws BadFormatException
   * @throws VimDriverException
   * @throws QuotaExceededException
   * @throws PluginException
   */
  @ApiOperation(
    value = "Deploying a Network Service Record from an existing NSD",
    notes =
        "The Network Service Record is created from the Network Service Descriptor specified in the id of the URL"
  )
  @RequestMapping(
    value = "{id}",
    method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public NetworkServiceRecord create(
      @PathVariable("id") String id,
      @RequestHeader(value = "project-id") String projectId,
      @RequestBody(required = false) JsonObject body)
      throws InterruptedException, ExecutionException, VimException, NotFoundException,
          BadFormatException, PluginException, MissingParameterException, BadRequestException,
          IOException, AlreadyExistingException {

    String monitoringIp = null;
    List keys = null;
    Map<String, Set<String>> vduVimInstances = null;
    Map<String, Configuration> configurations = null;

    log.debug("Json Body is" + body);
    if (body != null) {
      try {
        if (body.has("monitoringIp")) {
          monitoringIp = body.get("monitoringIp").getAsString();
        }
        if (body.has("keys")) {
          keys = gson.fromJson(body.getAsJsonArray("keys"), List.class);
        }
        if (body.has("vduVimInstances")) {
          Type mapType = new TypeToken<Map<String, Set<String>>>() {}.getType();
          vduVimInstances = gson.fromJson(body.getAsJsonObject("vduVimInstances"), mapType);
        }
        if (body.has("configurations")) {
          Type mapType = new TypeToken<Map<String, Configuration>>() {}.getType();
          configurations = gson.fromJson(body.get("configurations"), mapType);
        }
      } catch (Exception e) {
        log.error("The passed request body is not correct.");
        e.printStackTrace();
        throw new BadRequestException("The passed request body is not correct.");
      }
    }
    return networkServiceRecordManagement.onboard(
        id, projectId, keys, vduVimInstances, configurations, monitoringIp);
  }

  @RequestMapping(
    value = "{nsrId}/vnfd/{vnfdId}",
    method = RequestMethod.PUT,
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public NetworkServiceRecord scaleOut(
      @PathVariable("nsrId") String nsrId,
      @PathVariable("vnfdId") String vnfdId,
      @RequestHeader(value = "project-id") String projectId,
      @RequestBody(required = false) JsonObject jsonObject)
      throws NotFoundException, MissingParameterException, BadRequestException,
          InterruptedException, BadFormatException, ExecutionException {

    log.debug("Json Body is" + jsonObject);
    Type mapType = new TypeToken<Map<String, Configuration>>() {}.getType();

    String monitoringIp = null;
    if (jsonObject.has("monitoringIp")) {
      monitoringIp = jsonObject.get("monitoringIp").getAsString();
    }

    return networkServiceRecordManagement.scaleOutNsr(
        nsrId,
        vnfdId,
        projectId,
        gson.fromJson(jsonObject.getAsJsonArray("keys"), List.class),
        gson.fromJson(jsonObject.getAsJsonObject("vduVimInstances"), Map.class),
        gson.fromJson(jsonObject.get("configurations"), mapType),
        monitoringIp);
  }

  /**
   * This operation is used to remove a Network Service Record
   *
   * @param id : the id of Network Service Record
   */
  @ApiOperation(
    value = "Remove a Network Service Record",
    notes = "Removes the Network Service Record that has the id specified in the URL"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws InterruptedException, ExecutionException, NotFoundException, BadFormatException,
          WrongStatusException {
    networkServiceRecordManagement.delete(id, projectId);
  }

  /**
   * This operation is used to resume a failed Network Service Record
   *
   * @param id : the id of Network Service Record
   */
  @ApiOperation(
    value = "Resume a failed Network Service Record",
    notes = "The id in the URL specifies the Network Service Record that will be resumed"
  )
  @RequestMapping(value = "{id}/resume", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resume(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws InterruptedException, ExecutionException, NotFoundException, BadFormatException {
    networkServiceRecordManagement.resume(id, projectId);
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
  @ApiOperation(
    value = "Removing multiple Network Service Records",
    notes =
        "The list of ids in the Request Body specify the Network Service Records that will be deleted"
  )
  @RequestMapping(
    value = "/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void multipleDelete(
      @RequestBody @Valid List<String> ids, @RequestHeader(value = "project-id") String projectId)
      throws InterruptedException, ExecutionException, WrongStatusException, NotFoundException,
          BadFormatException {
    for (String id : ids) {
      networkServiceRecordManagement.delete(id, projectId);
    }
  }

  /**
   * This operation returns the list of Network Service Descriptor (NSD)
   *
   * @return List<NetworkServiceRecord>: the list of Network Service Descriptor stored
   */
  @ApiOperation(
    value = "Retrieving all Network Service Records",
    notes = "Returns all Network Service Records on the specified project"
  )
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
  @ApiOperation(
    value = "Retrieving a Network Service Record",
    notes = "The id in the URL belongs to the NSR that should be retrieved"
  )
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  public NetworkServiceRecord findById(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return networkServiceRecordManagement.query(id, projectId);
  }

  /**
   * This operation updates the Network Service Descriptor (NSD)
   *
   * @param networkServiceRecord : the Network Service Descriptor to be updated
   * @param id : the id of Network Service Descriptor
   * @return NetworkServiceRecord: the Network Service Descriptor updated
   */
  @ApiOperation(
    value = "Updating a Network Service Record",
    notes =
        "PUT request with the updated Network Service Record as JSON content in the request body and the id in the URL belongs to the NSR that shall be updated"
  )
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
   *     NSD
   */
  @ApiOperation(
    value = "Returns the Virtual Network Function Records of a NSR",
    notes = "Returns all the VNFRs that are part of the specified NSR"
  )
  @RequestMapping(
    value = "{id}/vnfrecords",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<VirtualNetworkFunctionRecord> getVirtualNetworkFunctionRecords(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
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
  @ApiOperation(
    value = "Return a single Virtual Network Function Record of a NSR ",
    notes = "The id of NSR and the id of the VNFR are specified in the URL"
  )
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
   * @param idNsr id of the NSR
   * @param idVnf id of the VNF
   * @throws NotFoundException
   */
  @ApiOperation(
    value = "Remove a single Virtual Network Function Record of a NSR",
    notes = "The id of NSR and the id of the VNFR are specified in the URL"
  )
  @RequestMapping(value = "{idNsr}/vnfrecords/{idVnf}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFRecord(
      @PathVariable("idNsr") String idNsr,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceRecordManagement.deleteVNFRecord(idNsr, idVnf, projectId);
  }

  /**
   * Removes multiple Virtual Network Function Records from the NSR repository
   *
   * @param ids: the id list of Virtual Network Function Records
   * @throws NotFoundException
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws WrongStatusException
   * @throws VimException
   */
  @ApiOperation(
    value = "Removing multiple Virtual Network Function Records",
    notes =
        "The list of ids in the Request Body specify the Virtual Network Function Records that will be deleted"
  )
  @RequestMapping(
    value = "{idNsr}/vnfrecords/multipledelete",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void vnfrMultipleDelete(
      @RequestBody @Valid List<String> ids,
      @PathVariable("idNsr") String idNsr,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    log.trace("NSR ID: " + idNsr + " Project ID: " + projectId + " VNFR IDs: " + ids);
    for (String idVnfr : ids) {
      networkServiceRecordManagement.deleteVNFRecord(idNsr, idVnfr, projectId);
    }
  }

  @ApiOperation(
    value = "Add a VNFC instance to a VDU of a VNFR",
    notes = "Specifies and adds a VNFC instance in the VDU inside a VNFR that is inside the NSR"
  )
  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  @SuppressWarnings("unchecked")
  public void postVNFCInstance(
      @RequestBody @Valid JsonObject body,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException, BadRequestException {
    if (!body.has("vnfComponent"))
      throw new BadRequestException(
          "The passed request body is not correct. It should include a field named: vnfComponent");

    VNFComponent component = retrieveVNFComponentFromRequest(body);
    List<String> vimInstanceNames = retrieveVimInstanceNamesFromRequest(body);

    log.trace("Received: " + component + "\nand\n" + vimInstanceNames);
    networkServiceRecordManagement.addVNFCInstance(
        id, idVnf, idVdu, component, "", projectId, vimInstanceNames);
  }

  @ApiOperation(
    value = "Add a VNFC instance to a random VDU of a VNFR",
    notes = "Adds a VNFC instance by only specifying the VNFR and the NSR"
  )
  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/vnfcinstances",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  @SuppressWarnings("unchecked")
  public void postVNFCInstance(
      @RequestBody @Valid JsonObject body,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException, BadRequestException {
    if (!body.has("vnfComponent"))
      throw new BadRequestException(
          "The passed request body is not correct. It should include a field named: vnfComponent");
    VNFComponent component = retrieveVNFComponentFromRequest(body);
    List<String> vimInstanceNames = retrieveVimInstanceNamesFromRequest(body);

    log.trace("Received: " + component + "\nand\n" + vimInstanceNames);
    networkServiceRecordManagement.addVNFCInstance(
        id, idVnf, component, projectId, vimInstanceNames);
  }

  /**
   * Helper method for extracting the VNFComponent object from API requests that add a VNFCInstance
   * to a VNFR.
   *
   * @param body the request's Json body. It looks like this: {"vnfComponent":{...},
   *     "vimInstanceNames":{...}}
   * @return the extracted VNFComponent
   */
  private VNFComponent retrieveVNFComponentFromRequest(JsonObject body) throws BadRequestException {
    VNFComponent component;
    try {
      component = gson.fromJson(body.getAsJsonObject("vnfComponent"), VNFComponent.class);
    } catch (Exception e) {
      log.error("Could not convert the request body's vnfComponent field into a VNFC");
      e.printStackTrace();
      throw new BadRequestException(
          "The vnfComponent field has to be a Json representation of a VNFC");
    }
    return component;
  }

  /**
   * Helper method for extracting the list of VimInstance names from API requests that add a
   * VNFCInstance to a VNFR.
   *
   * @param body the request's Json body. It looks like this: {"vnfComponent":{...},
   *     "vimInstanceNames":{...}}
   * @return the extracted list of VimInstance names
   */
  @SuppressWarnings("unchecked")
  private List<String> retrieveVimInstanceNamesFromRequest(JsonObject body)
      throws BadRequestException {
    List<String> vimInstanceNames = new LinkedList<>();
    if (body.has("vimInstanceNames")) {
      try {
        vimInstanceNames = gson.fromJson(body.getAsJsonArray("vimInstanceNames"), List.class);
      } catch (Exception e) {
        log.error("Could not convert the request body's vimInstanceNames field into a list");
        e.printStackTrace();
        throw new BadRequestException("The vimInstanceNames field has to be a Json list");
      }
    }
    return vimInstanceNames;
  }

  @ApiOperation(
    value = "Start an existing VNFC instance",
    notes = "Starts the specified VNFC Instance"
  )
  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances/{idVNFCI}/start",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public void startVNFCInstance(
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @PathVariable("idVNFCI") String idVNFCI,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    log.debug("start a VNF component instance");
    networkServiceRecordManagement.startVNFCInstance(id, idVnf, idVdu, idVNFCI, projectId);
  }

  @ApiOperation(value = "Stop a VNFC instance", notes = "Stops the specified VNFC Instance")
  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances/{idVNFCI}/stop",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public void stopVNFCInstance(
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @PathVariable("idVNFCI") String idVNFCI,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    log.debug("stop a VNF component instance");
    networkServiceRecordManagement.stopVNFCInstance(id, idVnf, idVdu, idVNFCI, projectId);
  }

  /**
   * Add a VNF Component (VNFC) in standby to a specific VNF. A standby VNFC is a VNFC instantiated
   * configured but not started.
   *
   * @param body VNF Component to add as JSON.
   * @param id NSR Id
   * @param idVnf VNF Id of the target VNF
   * @param idVdu VDU Id
   * @param projectId Project Id
   * @throws NotFoundException
   * @throws BadFormatException
   * @throws WrongStatusException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @ApiOperation(
    value = "Add a VNFC in standby",
    notes = "Instantiate and configure a new VNFC without start it, namely in standby"
  )
  @RequestMapping(
    value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}/vnfcinstances/standby",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  @SuppressWarnings("unchecked")
  public void postStandByVNFCInstance(
      @RequestBody @Valid JsonObject body,
      @PathVariable("id") String id,
      @PathVariable("idVnf") String idVnf,
      @PathVariable("idVdu") String idVdu,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, BadFormatException, WrongStatusException {
    VNFComponent component =
        gson.fromJson(body.getAsJsonObject("vnfComponent"), VNFComponent.class);
    List<String> vimInstanceNames =
        gson.fromJson(body.getAsJsonArray("vimInstanceNames"), List.class);
    log.debug(
        "PostStandByVNFCInstance received the component: "
            + component
            + "\nand\n"
            + vimInstanceNames);
    networkServiceRecordManagement.addVNFCInstance(
        id, idVnf, idVdu, component, "standby", projectId, vimInstanceNames);
  }

  /**
   * Execute the switch to standby action on a VNFR. This action starts the standby VNFC and sets
   * the failed VNFC in failed state. Eventually configures the dependent VNFRs.
   *
   * @param failedVnfcInstance failed VNFC instance as JSON
   * @param id NSR Id
   * @param idVnf VNFR Id
   * @param idVdu VDU Id
   * @param idVNFC VNFC Id of the standby VNFC
   * @param projectId Project Id
   * @throws NotFoundException
   * @throws BadFormatException
   * @throws WrongStatusException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @ApiOperation(
    value = "Execute the switch to standby action on a VNFR",
    notes =
        "This action starts the standby VNFC and sets the failed VNFC in failed state. Eventually configures the dependent VNFRs"
  )
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
      throws NotFoundException, BadFormatException, WrongStatusException, ExecutionException,
          InterruptedException {
    log.debug("switch to a standby component");
    networkServiceRecordManagement.switchToRedundantVNFCInstance(
        id, idVnf, idVdu, idVNFC, "standby", failedVnfcInstance, projectId);
  }

  @ApiOperation(
    value = "Remove a VNFC Instances from a VNFR",
    notes = "Removes a single specified VNFC Instance"
  )
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

  @ApiOperation(
    value = "Remove all VNFC Instances from all VDUs",
    notes = "Deletes all VNFC Instances from all VDUS in a VNFR"
  )
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

  @ApiOperation(
    value = "Remove all VNFC Instances from a VDU",
    notes = "Deletes all VNF Instances of the specified VDU"
  )
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
  @ApiOperation(value = "", notes = "", hidden = true)
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
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException {
    log.debug("Received: " + nfvMessage);
    networkServiceRecordManagement.executeAction(nfvMessage, id, idVnf, idVdu, idVNFCI, projectId);
  }

  @ApiOperation(
    value = "Add a Virtual Network Function Record",
    notes = "Add a new VNFR to an already existing NSR"
  )
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
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    nsr.getVnfr().add(vnfRecord);
    networkServiceRecordManagement.update(nsr, id, projectId);
    return vnfRecord;
  }

  @ApiOperation(
    value = "Update a Virtual Network Function Record in a NSR",
    notes = "Specify the ids of the VNFR and NSR which will be updated"
  )
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
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(idNsr, projectId);
    nsr.getVnfr().add(vnfRecord);
    networkServiceRecordManagement.update(nsr, idNsr, projectId);
    return vnfRecord;
  }

  @ApiOperation(
    value = "Restarts a VNFR in an NSR",
    notes = "Restarts a VNFR, rebuilding to a different image if specified"
  )
  @RequestMapping(
    value = "{idNsr}/vnfrecords/{idVnfr}/restart",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void restartVNFR(
      @RequestBody @Valid JsonObject body,
      @PathVariable("idNsr") String nsrId,
      @PathVariable("idVnfr") String vnfrId,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException, InterruptedException, BadRequestException, AlreadyExistingException,
          VimException, ExecutionException, PluginException, IOException, BadFormatException {
    log.debug("Received request for restarting a VNFR");
    if (!body.has("imageName") || !body.getAsJsonPrimitive("imageName").isString())
      throw new BadRequestException(
          "The passed JSON is not correct. It should include a string field named: imageName");
    String imageName = body.getAsJsonPrimitive("imageName").getAsString();
    //check if nsr exists
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(nsrId, projectId);
    networkServiceRecordManagement.restartVnfr(nsr, vnfrId, imageName, projectId);
  }

  /**
   * Returns a set of VNFDependency objects belonging to a specific NSR.
   *
   * @param id : the ID of NSR
   * @return the list of VNFDependency objects of the NSR
   */
  @ApiOperation(value = "Retrieve the VNF Dependencies of a NSR", notes = "")
  @RequestMapping(
    value = "{id}/vnfdependencies",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<VNFRecordDependency> getVNFDependencies(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    return nsr.getVnf_dependency();
  }

  @ApiOperation(
    value = "Retrieve a list of Dependency objects from the NSR",
    notes = "Returns all the Dependency object of the specified NSR"
  )
  @RequestMapping(
    value = "{id}/vnfdependenciesList",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<DependencyObject> getVNFDependenciesList(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
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
          if (vnfcInstance.getId().equals(vnfcId)) {
            return vnfcInstance.getHostname();
          }
        }
      }
    }
    return null;
  }

  @ApiOperation(
    value = "Retrieve VNF Dependencies from VNFR",
    notes = "Returns all VNF Dependencies that reference the specified VNFR"
  )
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

  @ApiOperation(
    value = "Remove a VNF Dependency from a NSR",
    notes = "Removes a VNF Dependency based on a VNFR it concerns"
  )
  @RequestMapping(value = "{idNsr}/vnfdependencies/{idVnfd}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteVNFDependency(
      @PathVariable("idNsr") String idNsr,
      @PathVariable("idVnfd") String idVnfd,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    networkServiceRecordManagement.deleteVNFDependency(idNsr, idVnfd, projectId);
  }

  @ApiOperation(
    value = "Add a VNF Dependency to a NSR",
    notes = "Adds a new VNF Dependency to the specified NSR"
  )
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

  @ApiOperation(
    value = "Updates a VNF Dependency in an NSR",
    notes = "Updates a VNF Dependency based on the if of the VNF it concerns"
  )
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
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(
    value = "{id}/pnfrecords",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public Set<PhysicalNetworkFunctionRecord> getPhysicalNetworkFunctionRecord(
      @PathVariable("id") String id, @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
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
  @ApiOperation(value = "", notes = "", hidden = true)
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
  @ApiOperation(value = "", notes = "", hidden = true)
  @RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePhysicalNetworkFunctionRecord(
      @PathVariable("id") String id,
      @PathVariable("id_pnf") String id_pnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    NetworkServiceRecord nsr = networkServiceRecordManagement.query(id, projectId);
    PhysicalNetworkFunctionRecord physicalNetworkFunctionRecord = findPNFD(nsr.getPnfr(), id_pnf);
    nsr.getPnfr().remove(physicalNetworkFunctionRecord);
  }

  /**
   * Stores the PhysicalNetworkFunctionRecord
   *
   * @param pDescriptor : The PhysicalNetworkFunctionRecord to be stored
   * @param id : The NSD id
   * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord stored
   */
  @ApiOperation(value = "", notes = "", hidden = true)
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
  @ApiOperation(value = "", notes = "", hidden = true)
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

  @ApiOperation(
    value = "Returns the history of the VNFRs of an NSR",
    notes = "Returns the history of the specified VNFR"
  )
  @RequestMapping(
    value = "{id}/vnfrecords/{id_vnf}/history",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public LinkedHashSet<HistoryLifecycleEvent> getHistory(
      @PathVariable("id") String id,
      @PathVariable("id_vnf") String id_vnf,
      @RequestHeader(value = "project-id") String projectId)
      throws NotFoundException {
    return (LinkedHashSet<HistoryLifecycleEvent>)
        networkServiceRecordManagement
            .getVirtualNetworkFunctionRecord(id, id_vnf, projectId)
            .getLifecycle_event_history();
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
