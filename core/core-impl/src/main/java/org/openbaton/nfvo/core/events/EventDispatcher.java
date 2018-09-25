/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

/** Created by lto on 03/06/15. */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.catalogue.util.BaseEntity;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.configuration.RabbitConfiguration;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.core.interfaces.EventSender;
import org.openbaton.nfvo.repositories.EventEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This class implements the interface {@Link EventDispatcher} so is in charge of handling the
 * de/registration of a EventEndpoint.
 *
 * <p>Moreover receives also internal events and dispatches them to the external applications.
 */
@Service
@Scope
class EventDispatcher
    implements ApplicationListener<EventNFVO>, org.openbaton.nfvo.core.interfaces.EventDispatcher {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  @Autowired private EventEndpointRepository eventEndpointRepository;
  @Autowired private ConfigurableApplicationContext context;
  @Autowired private org.openbaton.nfvo.core.interfaces.EventManagement eventManagement;

  @Override
  @RabbitListener(
      bindings =
          @QueueBinding(
              value =
                  @Queue(
                      value = RabbitConfiguration.QUEUE_NAME_EVENT_REGISTER,
                      durable = RabbitConfiguration.QUEUE_DURABLE,
                      autoDelete = RabbitConfiguration.QUEUE_AUTODELETE),
              exchange =
                  @Exchange(
                      value = RabbitConfiguration.EXCHANGE_NAME_OPENBATON,
                      ignoreDeclarationExceptions = "true",
                      type = RabbitConfiguration.EXCHANGE_TYPE_OPENBATON,
                      durable = RabbitConfiguration.EXCHANGE_DURABLE_OPENBATON),
              key = RabbitConfiguration.QUEUE_NAME_EVENT_REGISTER))
  public EventEndpoint register(String endpoint_json) throws MissingParameterException {

    EventEndpoint endpoint = gson.fromJson(endpoint_json, EventEndpoint.class);

    if (endpoint.getProjectId() == null) {
      throw new MissingParameterException("The Event Endpoint needs a project id");
    }

    if (!endpointAlreadyExists(endpoint)) return saveEventEndpoint(endpoint);
    return getEndpointAlreadyRegistered(endpoint);
  }

  private boolean endpointAlreadyExists(EventEndpoint endpoint) {
    Iterable<EventEndpoint> endpoints = eventEndpointRepository.findAll();

    for (EventEndpoint currentEndpoint : endpoints) {
      if (currentEndpoint.equals(endpoint)) {
        log.info("Such Endpoint already registered: " + currentEndpoint);
        return true;
      }
    }
    return false;
  }

  private EventEndpoint getEndpointAlreadyRegistered(EventEndpoint endpoint) {
    Iterable<EventEndpoint> endpoints = eventEndpointRepository.findAll();

    for (EventEndpoint currentEndpoint : endpoints) {
      if (currentEndpoint.equals(endpoint)) {
        return currentEndpoint;
      }
    }
    log.error("This endpoint is not already registered: " + endpoint);
    return null;
  }

  private EventEndpoint saveEventEndpoint(EventEndpoint endpoint) {

    EventEndpoint save = eventEndpointRepository.save(endpoint);
    log.info("Registered event endpoint" + save);

    eventManagement.removeUnreachableEndpoints();

    return save;
  }

  @Override
  public void onApplicationEvent(EventNFVO event) {
    log.trace("Received event: " + event);
    dispatchEvent(event);
  }

  @Override
  public void dispatchEvent(EventNFVO event) {
    log.trace("dispatching event to the world!!!");
    log.trace("event is: " + event);

    Iterable<EventEndpoint> endpoints = eventEndpointRepository.findAll();

    for (EventEndpoint endpoint : endpoints) {
      log.trace("Checking endpoint: " + endpoint);
      BaseEntity entity = (BaseEntity) event.getEventNFVO().getPayload();
      if (endpoint.getProjectId().equals("*")
          || endpoint.getProjectId().equals(entity.getProjectId())) {
        if (endpoint.getEvent().ordinal() == event.getEventNFVO().getAction().ordinal()) {
          if (endpoint.getVirtualNetworkFunctionId() != null
              && !endpoint.getVirtualNetworkFunctionId().equals("")) {
            if (event.getEventNFVO().getPayload() instanceof VirtualNetworkFunctionRecord) {
              VirtualNetworkFunctionRecord virtualNetworkFunctionRecord =
                  (VirtualNetworkFunctionRecord) event.getEventNFVO().getPayload();
              if (virtualNetworkFunctionRecord
                  .getId()
                  .equals(endpoint.getVirtualNetworkFunctionId())) {
                sendEvent(endpoint, event.getEventNFVO());
              }
            }
          } else if (endpoint.getNetworkServiceId() != null
              && !endpoint.getNetworkServiceId().equals("")) {
            if (event.getEventNFVO().getPayload() instanceof NetworkServiceRecord) {
              NetworkServiceRecord networkServiceRecord =
                  (NetworkServiceRecord) event.getEventNFVO().getPayload();
              if (networkServiceRecord.getId().equals(endpoint.getNetworkServiceId())) {
                sendEvent(endpoint, event.getEventNFVO());
              }
            }
          } else {
            sendEvent(endpoint, event.getEventNFVO());
          }
        }
      }
    }
  }

  private void sendEvent(EventEndpoint endpoint, ApplicationEventNFVO event) {
    EventSender sender =
        (EventSender) context.getBean(endpoint.getType().toString().toLowerCase() + "EventSender");
    log.trace("Sender is: " + sender.getClass().getSimpleName());
    sender.send(endpoint, event);
  }

  @Override
  @RabbitListener(
      bindings =
          @QueueBinding(
              value =
                  @Queue(
                      value = RabbitConfiguration.QUEUE_NAME_EVENT_UNREGISTER,
                      durable = RabbitConfiguration.QUEUE_DURABLE,
                      autoDelete = RabbitConfiguration.QUEUE_AUTODELETE),
              exchange =
                  @Exchange(
                      value = RabbitConfiguration.EXCHANGE_NAME_OPENBATON,
                      ignoreDeclarationExceptions = "true",
                      type = RabbitConfiguration.EXCHANGE_TYPE_OPENBATON,
                      durable = RabbitConfiguration.EXCHANGE_DURABLE_OPENBATON),
              key = RabbitConfiguration.QUEUE_NAME_EVENT_UNREGISTER))
  public void unregister(String id) throws NotFoundException {
    eventManagement.removeUnreachableEndpoints();
    EventEndpoint endpoint = eventEndpointRepository.findFirstById(id);
    if (endpoint == null) throw new NotFoundException("No event found with ID " + id);
    log.info("Removing EventEndpoint with id: " + id);
    eventEndpointRepository.delete(id);
  }
}
