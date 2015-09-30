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

package org.openbaton.nfvo.core.events.senders;

import com.google.gson.Gson;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.nfvo.core.interfaces.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.concurrent.Future;

/**
 * Created by lto on 01/07/15.
 */
@Service
@Scope
public class JmsEventSender implements EventSender {

    @Autowired
    private JmsTemplate jmsTemplate;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @Async
    public Future<Void> send(EventEndpoint endpoint, final ApplicationEventNFVO event) {

        log.debug("Sending message: " + event + " to endpoint: " + endpoint);
        log.info("Sending message: " + event.getAction() + " to endpoint: " + endpoint.getName());
        final String json = "{action:'" + event.getAction() + "',payload:'" + new Gson().toJson(event.getPayload()) + "'}";
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage objectMessage = session.createTextMessage(json);
                return objectMessage;
            }
        };
        jmsTemplate.send(endpoint.getEndpoint(), messageCreator);

        return new AsyncResult<>(null);
    }
}
