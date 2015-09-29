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

package org.openbaton.nfvo.vnfm_reg.impl.register;

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Created by lto on 27/05/15.
 */
@Component
public class JmsRegister extends VnfmRegister {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @JmsListener(destination = "vnfm-register", containerFactory = "queueJmsContainerFactory")
    public void addManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        if (endpoint.getEndpointType() == null) {
            endpoint.setEndpointType(EndpointType.JMS);
        }
        log.debug("Received: " + endpoint);
        this.register(endpoint);
    }

    @Override
    @JmsListener(destination = "vnfm-unregister", containerFactory = "queueJmsContainerFactory")
    public void removeManagerEndpoint(@Payload VnfmManagerEndpoint endpoint) {
        log.debug("Unregistering: " + endpoint);
        this.unregister(endpoint);
    }
}
