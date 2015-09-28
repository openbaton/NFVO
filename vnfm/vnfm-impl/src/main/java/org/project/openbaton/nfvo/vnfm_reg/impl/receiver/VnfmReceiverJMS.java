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

package org.project.openbaton.nfvo.vnfm_reg.impl.receiver;

import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.exceptions.NotFoundException;
import org.project.openbaton.exceptions.VimException;
import org.project.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;

/**
 * Created by lto on 26/05/15.
 */
@Service
public class VnfmReceiverJMS implements VnfmReceiver {

    @Autowired
    private org.project.openbaton.vnfm.interfaces.manager.VnfmManager vnfmManager;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @JmsListener(destination = "vnfm-core-actions", containerFactory = "queueJmsContainerFactory", concurrency = "20")
    public void actionFinished(@Payload Object nfvMessage, @Header(name = JmsHeaders.REPLY_TO, required = false) Destination tempDestination) throws NotFoundException, VimException {
        log.debug("CORE: Received: " + nfvMessage);
        NFVMessage message = null;
        try {
            message = (NFVMessage) ((org.apache.activemq.command.ActiveMQObjectMessage) nfvMessage).getObject();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        log.debug("----------Executing ACTION: " + message.getAction());
        vnfmManager.executeAction(message, tempDestination);
        log.debug("-----------Finished ACTION: " + message.getAction());

    }
}
