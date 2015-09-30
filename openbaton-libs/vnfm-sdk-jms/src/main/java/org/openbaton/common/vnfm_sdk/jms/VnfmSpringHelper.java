/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

package org.openbaton.common.vnfm_sdk.jms;

import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by lto on 23/09/15.
 */
@Service
@Scope
public class VnfmSpringHelper extends VnfmHelper {

    private static final String nfvoQueue = "vnfm-core-actions";

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    private void init() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/conf.properties"));


    }

    public void sendMessageToQueue(String sendToQueueName, final Serializable message) {
        log.trace("Sending message: " + message + " to Queue: " + sendToQueueName);

        MessageCreator messageCreator;

        if (message instanceof java.lang.String)
            messageCreator = getTextMessageCreator((String) message);
        else
            messageCreator = getObjectMessageCreator(message);

        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setPubSubNoLocal(false);
        jmsTemplate.send(sendToQueueName, messageCreator);
    }

    public MessageCreator getTextMessageCreator(final String string) {
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage objectMessage = session.createTextMessage(string);
                return objectMessage;
            }
        };
        return messageCreator;
    }

    public MessageCreator getObjectMessageCreator(final Serializable message) {
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage(message);
                return objectMessage;
            }
        };
        return messageCreator;
    }

    /**
     * This method should be used for receiving text message from EMS
     *
     * resp = {
     * 'output': out,          // the output of the command
     * 'err': err,             // the error outputs of the commands
     * 'status': status        // the exit status of the command
     * }
     *
     * @param queueName
     * @return
     * @throws JMSException
     */
    public String receiveTextFromQueue(String queueName) throws JMSException {
        return ((TextMessage) this.jmsTemplate.receive(queueName)).getText();
    }

    @Override
    public void sendToNfvo(final NFVMessage nfvMessage) {
        sendMessageToQueue(nfvoQueue, nfvMessage);
    }

    @Override
    public NFVMessage sendAndReceive(NFVMessage message) throws Exception {
        Message response = this.jmsTemplate.sendAndReceive(nfvoQueue, getObjectMessageCreator(message));
        return (NFVMessage) ((ObjectMessage) response).getObject();
    }

}
