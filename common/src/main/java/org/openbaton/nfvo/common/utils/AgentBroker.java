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

package org.openbaton.nfvo.common.utils;

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.interfaces.Receiver;
import org.openbaton.nfvo.common.interfaces.Sender;
import org.openbaton.nfvo.common.utils.jms.RabbitReceiver;
import org.openbaton.nfvo.common.utils.jms.RabbitSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by tce on 14.08.15. */
@Service
@Scope
public class AgentBroker {

  @Autowired private ConfigurableApplicationContext context;

  public Sender getSender(EndpointType endpointType) throws NotFoundException {
    switch (endpointType) {
      case JMS:
        return (RabbitSender) context.getBean("rabbitSender");
      case REST:
        //TODO RestSender
      default:
        throw new NotFoundException("no type sender found");
    }
  }

  public Receiver getReceiver(EndpointType endpointType) throws NotFoundException {
    switch (endpointType) {
      case JMS:
        return (RabbitReceiver) context.getBean("rabbitReceiver");
      case REST:
        //TODO RestReceiver
      default:
        throw new NotFoundException("no type Receiver found");
    }
  }
}
