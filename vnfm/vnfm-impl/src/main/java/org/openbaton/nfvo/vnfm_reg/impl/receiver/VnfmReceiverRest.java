/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.vnfm_reg.impl.receiver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrAllocateResourcesMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScalingMessage;
import org.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.openbaton.vnfm.interfaces.state.VnfStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/v1/")
public class VnfmReceiverRest implements VnfmReceiver {

  @Autowired private Gson gson;
  @Autowired private VnfStateHandler vnfStateHandler;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public String actionFinished(@RequestBody String nfvMessage)
      throws ExecutionException, InterruptedException {

    log.debug("NFVO - core module received (via REST): " + nfvMessage);
    NFVMessage message = gson.fromJson(nfvMessage, NFVMessage.class);

    Future<NFVMessage> res = vnfStateHandler.executeAction(message);
    return gson.toJson(res.get());
  }

  @RequestMapping(
    value = "vnfm-core-actions-reply",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public void actionFinishedRest(@RequestBody JsonObject nfvMessage)
      throws InterruptedException, ExecutionException {

    this.actionFinished(gson.toJson(nfvMessage));
  }

  @RequestMapping(
    value = "vnfm-core-actions",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.OK)
  public void actionFinishedVoidRest(@RequestBody JsonObject nfvMessage)
      throws InterruptedException, ExecutionException {
    this.actionFinishedVoid(gson.toJson(nfvMessage));
  }

  @Override
  public void actionFinishedVoid(String nfvMessage)
      throws ExecutionException, InterruptedException {
    log.debug("NFVO - core module received (via REST): " + nfvMessage);
    NFVMessage message = gson.fromJson(nfvMessage, NFVMessage.class);
    vnfStateHandler.executeAction(message).get();
  }

  @RequestMapping(
    value = "vnfm-core-grant",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NFVMessage grantLifecycleOperation(@RequestBody VnfmOrGenericMessage message)
      throws ExecutionException, InterruptedException {

    log.debug("NFVO - core module received (via REST):" + message);

    return vnfStateHandler.executeAction(message).get();
  }

  @RequestMapping(
    value = "vnfm-core-allocate",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NFVMessage allocate(@RequestBody VnfmOrAllocateResourcesMessage message)
      throws ExecutionException, InterruptedException {

    return vnfStateHandler.executeAction(message).get();
  }

  @RequestMapping(
    value = "vnfm-core-scale",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NFVMessage scale(@RequestBody VnfmOrScalingMessage message)
      throws InterruptedException, ExecutionException {
    return vnfStateHandler.executeAction(message).get();
  }

  public Gson getGson() {
    return gson;
  }

  public void setGson(Gson gson) {
    this.gson = gson;
  }
}
