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

package org.openbaton.nfvo.common.utils.jms;

import org.openbaton.nfvo.common.interfaces.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by tce on 14.08.15.
 */
@Service
@Scope
public class RabbitReceiver implements Receiver {
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private RabbitTemplate rabbitTemplate;

  @Override
  public Object receive(String queue) {
    return rabbitTemplate.receiveAndConvert(queue);
  }
}
