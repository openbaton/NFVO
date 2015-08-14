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

package org.project.openbaton.nfvo.vnfm_reg.impl.sender;

import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.common.utils.jms.JmsSender;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by lto on 03/06/15.
 */
@Service
@Scope
public class JmsVnfmSender implements VnfmSender{

    @Autowired
    private JmsSender jmsSender;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void sendCommand(final CoreMessage coreMessage, final VnfmManagerEndpoint endpoint) {
//        this.sendToQueue(coreMessage, endpoint.getType());
        String destinationName = "core-" + endpoint.getType() + "-actions";
        jmsSender.send(destinationName, coreMessage);
    }

//    public void sendToQueue(final CoreMessage coreMessage, String type) {
//        String destinationName = "core-" + type + "-actions";
//        log.debug("Sending message: " + coreMessage.getAction() + " to Queue: " + destinationName);
//        log.trace("Sending message: " + coreMessage + " to Queue: " + destinationName);
//        MessageCreator messageCreator = new MessageCreator() {
//            @Override
//            public Message createMessage(Session session) throws JMSException {
//                ObjectMessage objectMessage = session.createObjectMessage(coreMessage);
//                return objectMessage;
//            }
//        };
//
//        jmsTemplate.send(destinationName, messageCreator);
//    }
//    public void sendToTopic(final CoreMessage coreMessage, String destinationTopicName, final String selector) {
//        log.debug("Sending message: " + coreMessage.getAction() + " to Topic: " + destinationTopicName + " where selector is: type=\'" + selector + "\'");
//        log.trace("Sending message: " + coreMessage + " to Topic: " + destinationTopicName + " where selector is: type=\'" + selector + "\'");
//        MessageCreator messageCreator = new MessageCreator() {
//            @Override
//            public Message createMessage(Session session) throws JMSException {
//                ObjectMessage objectMessage = session.createObjectMessage(coreMessage);
//                log.trace("SELECTOR: type=\'"+ selector+ "\'");
//                objectMessage.setStringProperty("type", selector );
//                return objectMessage;
//            }
//        };
//        jmsTemplate.setPubSubDomain(true);
//        jmsTemplate.setPubSubNoLocal(true);
//        jmsTemplate.send(destinationTopicName, messageCreator);
//
//    }
}
