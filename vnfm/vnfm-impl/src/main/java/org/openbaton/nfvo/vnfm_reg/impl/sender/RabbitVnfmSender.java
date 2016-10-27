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
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 03/06/15.
 */
@Service
@Scope
public class RabbitVnfmSender implements VnfmSender {

  @Autowired private Gson gson;

  @Autowired private RabbitTemplate rabbitTemplate;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void sendCommand(final NFVMessage nfvMessage, final VnfmManagerEndpoint endpoint) {
    String destinationName = "nfvo." + endpoint.getType() + ".actions";
    log.debug(
        "Sending NFVMessage with action: "
            + nfvMessage.getAction()
            + " to destination: "
            + destinationName);
    log.trace("nfvMessage is: " + nfvMessage);
    log.trace("gson is: " + gson);
    log.trace("RabbitTemplate is: " + rabbitTemplate);
    String json = null;
    try {
      json = gson.toJson(nfvMessage);
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.trace("Json is: " + json);
    rabbitTemplate.convertAndSend(destinationName, json);
  }

  @Override
  public void sendCommand(final NFVMessage nfvMessage, String tempDestination) {
    log.trace(
        "Sending NFVMessage with action: "
            + nfvMessage.getAction()
            + " to tempQueue: "
            + tempDestination);

    rabbitTemplate.setReplyQueue(new Queue(tempDestination));
    rabbitTemplate.convertAndSend(tempDestination, gson.toJson(nfvMessage));
  }
}
