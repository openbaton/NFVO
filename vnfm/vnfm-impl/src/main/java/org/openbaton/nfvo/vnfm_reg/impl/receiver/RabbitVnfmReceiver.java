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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.common.configuration.RabbitConfiguration;
import org.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.openbaton.vnfm.interfaces.state.VnfStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitVnfmReceiver implements VnfmReceiver {

  @Autowired private Gson gson;

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private VnfStateHandler stateHandler;

  @Override
  @RabbitListener(
    bindings =
        @QueueBinding(
          value =
              @Queue(
                value = RabbitConfiguration.QUEUE_NAME_VNFM_CORE_ACTIONS_REPLY,
                durable = RabbitConfiguration.QUEUE_DURABLE,
                autoDelete = RabbitConfiguration.QUEUE_AUTODELETE
              ),
          exchange =
              @Exchange(
                value = RabbitConfiguration.EXCHANGE_NAME_OPENBATON,
                ignoreDeclarationExceptions = "true",
                type = RabbitConfiguration.EXCHANGE_TYPE_OPENBATON,
                durable = RabbitConfiguration.EXCHANGE_DURABLE_OPENBATON
              ),
          key = RabbitConfiguration.QUEUE_NAME_VNFM_CORE_ACTIONS_REPLY
        )
  )
  public String actionFinished(String nfvMessage)
      throws NotFoundException, VimException, ExecutionException, InterruptedException {
    NFVMessage message = gson.fromJson(nfvMessage, NFVMessage.class);
    log.debug("NFVO - core module received (via MB): " + message.getAction());

    log.debug("Received ACTION: " + message.getAction());
    Future<NFVMessage> res = stateHandler.executeAction(message);
    String result = gson.toJson(res.get());
    log.debug("Concluded ACTION: " + message.getAction());

    return result;
  }

  @Override
  @RabbitListener(
    bindings =
        @QueueBinding(
          value =
              @Queue(
                value = RabbitConfiguration.QUEUE_NAME_VNFM_CORE_ACTIONS,
                durable = RabbitConfiguration.QUEUE_DURABLE,
                autoDelete = RabbitConfiguration.QUEUE_AUTODELETE
              ),
          exchange =
              @Exchange(
                value = RabbitConfiguration.EXCHANGE_NAME_OPENBATON,
                ignoreDeclarationExceptions = "true",
                type = RabbitConfiguration.EXCHANGE_TYPE_OPENBATON,
                durable = RabbitConfiguration.EXCHANGE_DURABLE_OPENBATON
              ),
          key = RabbitConfiguration.QUEUE_NAME_VNFM_CORE_ACTIONS
        )
  )
  public void actionFinishedVoid(String nfvMessage)
      throws NotFoundException, VimException, ExecutionException, InterruptedException {
    NFVMessage message = gson.fromJson(nfvMessage, NFVMessage.class);
    log.debug("NFVO - core module received (via MB)" + message.getAction());

    log.debug("----------Executing ACTION: " + message.getAction());
    stateHandler.executeAction(message).get();
    log.debug("-----------Finished ACTION: " + message.getAction());
  }
}
