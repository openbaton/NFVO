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

package org.openbaton.nfvo.common.utils.jms;

import org.openbaton.nfvo.common.interfaces.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import java.io.Serializable;

/**
 * Created by tce on 13.08.15.
 */
@Service
@Scope
public class RabbitSender implements Sender {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private AmqpTemplate amqpTemplate;

  @Override
  public void send(String destination, final Serializable message) {
    log.trace("Sending message: " + message + " to Queue: " + destination);
    amqpTemplate.convertAndSend(destination, message);
  }

  @Override
  public Serializable receiveObject(String destination) {
    return (Serializable) amqpTemplate.receiveAndConvert(destination);
  }

  @Override
  public String receiveText(String destination) {
    return (String) amqpTemplate.receiveAndConvert(destination);
  }
}
