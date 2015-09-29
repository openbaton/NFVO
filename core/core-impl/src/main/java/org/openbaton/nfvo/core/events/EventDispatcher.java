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

package org.openbaton.nfvo.core.events;

/**
 * Created by lto on 03/06/15.
 */

import org.openbaton.nfvo.core.interfaces.EventSender;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.EventEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * This class implements the interface {@Link EventDispatcher} so is in charge
 * of handling the de/registration of a EventEndpoint.
 *
 * Moreover receives also internal events and dispatches them to the external applications.
 */
@Service
@Scope
@EnableJms
class EventDispatcher implements ApplicationListener<ApplicationEventNFVO>, org.openbaton.nfvo.core.interfaces.EventDispatcher {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private EventEndpointRepository eventEndpointRepository;
    @Autowired
    private ConfigurableApplicationContext context;

    @Override
    @JmsListener(destination = "event-register", containerFactory = "queueJmsContainerFactory")
    public EventEndpoint register(@Payload EventEndpoint endpoint) {
        return eventEndpointRepository.save(endpoint);
    }

    @Override
    public void onApplicationEvent(ApplicationEventNFVO event) {
        log.debug("Received event: " + event);
        switch (event.getAction()) {
            case INSTANTIATE_FINISH:
                break;
            case ALLOCATE_RESOURCES:
                break;
            case ERROR:
                break;
            case RELEASE_RESOURCES:
                break;
            case INSTANTIATE:
                break;
            case MODIFY:
                break;
        }

        dispatchEvent(event);
    }

    @Override
    public void dispatchEvent(ApplicationEventNFVO event) {
        log.debug("dispatching event to the world!!!");
        log.debug("event is: " + event);

        Iterable<EventEndpoint> endpoints = eventEndpointRepository.findAll();

        for (EventEndpoint endpoint : endpoints) {
            log.debug("Checking endpoint: " + endpoint);
            if (endpoint.getEvent().ordinal() == event.getAction().ordinal()) {
                if (endpoint.getVirtualNetworkFunctionId() != null) {
                    if (event.getPayload() instanceof VirtualNetworkFunctionRecord) {
                        if (((VirtualNetworkFunctionRecord) event.getPayload()).getId().equals(endpoint.getVirtualNetworkFunctionId())) {
                            log.debug("dispatching event to: " + endpoint);
                            sendEvent(endpoint, event);
                        }
                    }
                } else if (endpoint.getNetworkServiceId() != null) {
                    if (event.getPayload() instanceof NetworkServiceRecord) {
                        if (((NetworkServiceRecord) event.getPayload()).getId().equals(endpoint.getNetworkServiceId())) {
                            log.debug("dispatching event to: " + endpoint);
                            sendEvent(endpoint, event);
                        }
                    }
                } else {
                    log.debug("dispatching event to: " + endpoint);
                    sendEvent(endpoint, event);
                }
            }
        }

    }

    private void sendEvent(EventEndpoint endpoint, ApplicationEventNFVO event) {
        EventSender sender = (EventSender) context.getBean(endpoint.getType().toString().toLowerCase() + "EventSender");
        log.trace("Sender is: " + sender.getClass().getSimpleName());
        try {
            sender.send(endpoint, event);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while dispatching event " + event);
        }
    }

    @Override
    @JmsListener(destination = "event-unregister", containerFactory = "queueJmsContainerFactory")
    public void unregister(String id) throws NotFoundException {
        eventEndpointRepository.delete(eventEndpointRepository.findOne(id));
    }

}
