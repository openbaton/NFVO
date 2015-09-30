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

import org.openbaton.nfvo.common.interfaces.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.io.Serializable;

/**
 * Created by tce on 13.08.15.
 */
@Service
@Scope
public class JmsSender implements Sender {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void send(String destination, final Serializable message) {
        log.trace("Sending message: " + message + " to Queue: " + destination);
        MessageCreator messageCreator = getMessageCreator(message);
        jmsTemplate.send(destination, messageCreator);
    }

    public void send(Destination destination, final Serializable message) {
        log.trace("Sending message: " + message + " to Queue: " + destination);
        MessageCreator messageCreator = getMessageCreator(message);
        jmsTemplate.send(destination, messageCreator);
    }

    private MessageCreator getMessageCreator(final Serializable message) {
        return new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message msg;
                if (message instanceof java.lang.String)
                    msg = session.createTextMessage((String) message);
                else
                    msg = session.createObjectMessage(message);
                return msg;
            }
        };
    }

    @Override
    public Serializable receiveObject(String destination) throws JMSException {
        return ((ObjectMessage) jmsTemplate.receive(destination)).getObject();
    }

    @Override
    public String receiveText(String destination) throws JMSException {
        return ((TextMessage) jmsTemplate.receive(destination)).getText();
    }
}
