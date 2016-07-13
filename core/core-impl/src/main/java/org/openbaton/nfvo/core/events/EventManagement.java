package org.openbaton.nfvo.core.events;

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.nfvo.repositories.EventEndpointRepository;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * Created by lto on 10/03/16.
 */
@Service
@Scope
@EnableAsync
@ConfigurationProperties
public class EventManagement implements org.openbaton.nfvo.core.interfaces.EventManagement {

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String brokerIp;

  @Autowired private EventEndpointRepository eventEndpointRepository;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

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
          if (!RabbitManager.getQueues(brokerIp.trim(), username, password, managementPort)
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
  public EventEndpoint query(String id, String projectId) {
    EventEndpoint endpoint = eventEndpointRepository.findFirstById(id);
    if (endpoint.getProjectId().equals(projectId)) return endpoint;
    throw new UnauthorizedUserException(
        "Event not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
  }

  @Override
  public Iterable<EventEndpoint> queryByProjectId(String projectId) {
    return eventEndpointRepository.findByProjectId(projectId);
  }
}
