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

package org.openbaton.nfvo.vnfm_reg.impl.receiver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.*;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.common.configuration.GsonDeserializerNFVMessage;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

/**
 * Created by lto on 26/05/15.
 */
@RestController
@RequestMapping("/admin/v1/")
public class VnfmReceiverRest implements VnfmReceiver {

  @Autowired private NetworkServiceRecordRepository networkServiceRecordRepository;

  private Gson mapper =
      new GsonBuilder()
          .registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage())
          .setPrettyPrinting()
          .create();

  @Autowired private VNFRRepository vnfrRepository;

  @Autowired private VNFLifecycleOperationGranting vnfLifecycleOperationGranting;

  @Autowired private ResourceManagement resourceManagement;

  @Autowired private VnfmManager vnfmManager;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public String actionFinished(@RequestBody String nfvMessage)
      throws NotFoundException, VimException, ExecutionException, InterruptedException {

    //TODO rewrite this or better remove it
    log.debug("CORE: Received: " + nfvMessage);
    String action =
        mapper.fromJson(mapper.toJson(nfvMessage), JsonObject.class).get("action").getAsString();
    NFVMessage message = mapper.fromJson(mapper.toJson(nfvMessage), NFVMessage.class);

    return vnfmManager.executeAction(message);
  }

  @RequestMapping(
    value = "vnfm-core-actions-reply",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public void actionFinishedRest(@RequestBody JsonObject nfvMessage)
      throws InterruptedException, ExecutionException, VimException, NotFoundException {

    this.actionFinished(mapper.toJson(nfvMessage));
  }

  @RequestMapping(
    value = "vnfm-core-actions",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public void actionFinishedVoidRest(@RequestBody JsonObject nfvMessage)
      throws InterruptedException, ExecutionException, VimException, NotFoundException {
    this.actionFinishedVoid(mapper.toJson(nfvMessage));
  }

  @Override
  public void actionFinishedVoid(String nfvMessage)
      throws NotFoundException, VimException, ExecutionException, InterruptedException {
    log.debug("CORE: Received: " + nfvMessage);
    String action = mapper.fromJson(nfvMessage, JsonObject.class).get("action").getAsString();
    NFVMessage message;
    if (action.equals("INSTANTIATE")) {
      message = mapper.fromJson(nfvMessage, VnfmOrInstantiateMessage.class);
      log.trace("DESERIALIZED: " + message);
    } else if (action.equals("SCALED")) {
      message = mapper.fromJson(nfvMessage, VnfmOrScaledMessage.class);
      log.trace("DESERIALIZED: " + message);
    } else {
      message = mapper.fromJson(nfvMessage, VnfmOrGenericMessage.class);
      log.trace("DESERIALIZED: " + message);
    }
    vnfmManager.executeAction(message);
  }

  @RequestMapping(
    value = "vnfm-core-grant",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NFVMessage grantLifecycleOperation(@RequestBody VnfmOrGenericMessage message)
      throws VimException, PluginException, ExecutionException, InterruptedException {

    log.debug("CORE: Received: " + message);

    Gson gson = new GsonBuilder().create();
    String executeReturned = vnfmManager.executeAction(message);
    return mapper.fromJson(executeReturned, OrVnfmGrantLifecycleOperationMessage.class);
  }

  @RequestMapping(
    value = "vnfm-core-allocate",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NFVMessage allocate(@RequestBody VnfmOrAllocateResourcesMessage message)
      throws VimException {

    try {
      return mapper.fromJson(vnfmManager.executeAction(message), OrVnfmGenericMessage.class);
    } catch (ExecutionException e1) {
      e1.printStackTrace();
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
    return null;
  }

  @RequestMapping(
    value = "vnfm-core-scale",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NFVMessage scale(@RequestBody VnfmOrScalingMessage message)
      throws InterruptedException, ExecutionException, VimException, NotFoundException {
    return mapper.fromJson(vnfmManager.executeAction(message), OrVnfmGenericMessage.class);
  }

  private VirtualNetworkFunctionRecord saveVirtualNetworkFunctionRecord(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    if (virtualNetworkFunctionRecord.getId() == null)
      return networkServiceRecordRepository.addVnfr(
          virtualNetworkFunctionRecord, virtualNetworkFunctionRecord.getParent_ns_id());
    else return vnfrRepository.save(virtualNetworkFunctionRecord);
  }
}
