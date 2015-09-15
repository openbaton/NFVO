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

import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
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
    public void sendCommand(final NFVMessage nfvMessage, final VnfmManagerEndpoint endpoint) {
//        this.sendToQueue(coreMessage, endpoint.getType());
        String destinationName = "core-" + endpoint.getType() + "-actions";
        jmsSender.send(destinationName, nfvMessage);
    }

    @Override
    public void sendCommand(final NFVMessage nfvMessage, String tempDestination) {
        jmsSender.send(tempDestination,nfvMessage);
    }

}
