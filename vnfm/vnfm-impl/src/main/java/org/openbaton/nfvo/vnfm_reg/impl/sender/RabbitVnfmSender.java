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

package org.openbaton.nfvo.vnfm_reg.impl.sender;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import org.openbaton.catalogue.nfvo.Endpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/** Created by lto on 03/06/15. */
@Service
@Scope
@EnableAsync
public class RabbitVnfmSender implements VnfmSender {

  @Autowired private Gson gson;

  @Autowired private RabbitTemplate rabbitTemplate;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  @Async
  public Future<NFVMessage> sendCommand(final NFVMessage nfvMessage, final Endpoint endpoint)
      throws BadFormatException {
    String destinationName = endpoint.getEndpoint();
    log.debug(
        "Sending NFVMessage with action: "
            + nfvMessage.getAction()
            + " to destination: "
            + destinationName);
    log.trace("nfvMessage is: " + nfvMessage);
    String json = null;
    try {
      json = gson.toJson(nfvMessage);
    } catch (Exception e) {
      e.printStackTrace();
    }
    rabbitTemplate.setReplyTimeout(-1);
    Object receive =
        rabbitTemplate.convertSendAndReceive("openbaton-exchange", destinationName, json);
    log.trace("Received: " + receive);
    String str;
    if (receive instanceof byte[]) {
      str = new String((byte[]) receive, StandardCharsets.UTF_8);
    } else {
      str = (String) receive;
    }
    NFVMessage result;
    try {
      result = gson.fromJson(str, NFVMessage.class);
    } catch (Exception e) {
      throw new BadFormatException(e);
    }
    return new AsyncResult<>(result);
  }
}
