/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
 *
 */

package org.openbaton.nfvo.core.events;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Future;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.utils.rabbit.RabbitManager;
import org.openbaton.nfvo.repositories.EventEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/** Created by lto on 10/03/16. */
@Service
@Scope
@ConfigurationProperties
public class EventManagement implements org.openbaton.nfvo.core.interfaces.EventManagement {

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String brokerIp;

  @Autowired private EventEndpointRepository eventEndpointRepository;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${spring.rabbitmq.virtual-host:/}")
  private String virtualHost;

  @Value("${nfvo.rabbit.management.port:15672}")
  private int managementPort;

  private static boolean pingHost(String host, int port, int timeout) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
      return true;
    } catch (IOException ignored) {
      return false; // Either timeout or unreachable or failed DNS lookup.
    }
  }

  @Override
  @Async
  public Future<Void> removeUnreachableEndpoints() {
    log.debug("Checking for unreachable events");
    Iterable<EventEndpoint> eventEndpoints = eventEndpointRepository.findAll();
    for (EventEndpoint eventEndpoint : eventEndpoints) {
      if (eventEndpoint.getType().ordinal() == EndpointType.REST.ordinal()) {
        //TODO check if endpoint is reachable
        try {
          URL url = new URL(eventEndpoint.getEndpoint());
          if (!pingHost(url.getHost(), url.getPort(), 3000)) {
            log.warn("Event endpoint " + eventEndpoint + " is not there anymore.");
            eventEndpointRepository.delete(eventEndpoint.getId());
          }
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      } else if (eventEndpoint.getType().ordinal() == EndpointType.RABBIT.ordinal()) {
        try {
          if (!RabbitManager.getQueues(
                  brokerIp.trim(), username, password, virtualHost, managementPort)
              .contains(eventEndpoint.getEndpoint())) {
            log.warn("Event endpoint " + eventEndpoint + " is not there anymore.");
            eventEndpointRepository.delete(eventEndpoint.getId());
          }
        } catch (IOException e) {
          log.error("Error while retrieving queues: " + e.getLocalizedMessage());
        }
      }
    }
    return new AsyncResult<>(null);
  }

  @Override
  public Iterable<EventEndpoint> query(String projectId) {
    return eventEndpointRepository.findByProjectId(projectId);
  }

  @Override
  public EventEndpoint query(String id, String projectId) throws NotFoundException {
    EventEndpoint endpoint = eventEndpointRepository.findFirstByIdAndProjectId(id, projectId);
    if (endpoint == null) {
      endpoint = eventEndpointRepository.findFirstByIdAndProjectId(id, "*");
      if (endpoint == null) throw new NotFoundException("No Event found with ID " + id);
    }
    return endpoint;
  }

  @Override
  public Iterable<EventEndpoint> queryByProjectId(String projectId) {
    return eventEndpointRepository.findByProjectId(projectId);
  }
}
