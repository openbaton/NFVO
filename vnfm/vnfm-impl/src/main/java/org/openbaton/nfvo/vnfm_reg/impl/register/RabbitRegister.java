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
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.nfvo.vnfm_reg.VnfmRegister;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by lto on 27/05/15.
 */
@Service
@ConfigurationProperties
public class RabbitRegister extends VnfmRegister {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.rabbit.management.port:15672}")
  private int managementPort;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String brokerIp;

  @Override
  public void addManagerEndpoint(String endpoint_json) {

    VnfmManagerEndpoint endpoint = gson.fromJson(endpoint_json, VnfmManagerEndpoint.class);

    if (endpoint.getEndpointType() == null) {
      endpoint.setEndpointType(EndpointType.RABBIT);
    }
    log.info("Registering endpoint of type: " + endpoint.getType());
    try {
      this.register(endpoint);
    } catch (AlreadyExistingException e) {
      log.warn(e.getLocalizedMessage());
    }
  }

  @Override
  public void removeManagerEndpoint(String endpoint_json) {

    VnfmManagerEndpoint endpoint = gson.fromJson(endpoint_json, VnfmManagerEndpoint.class);
    log.debug("Unregistering: " + endpoint);
    for (VnfmManagerEndpoint vnfmManagerEndpoint : vnfmEndpointRepository.findAll())
      if (vnfmManagerEndpoint.getEndpoint().equals(endpoint.getEndpoint())) {
        this.unregister(vnfmManagerEndpoint);
        break;
      }
  }

  @Scheduled(initialDelay = 30000, fixedDelay = 20000)
  public void checkHeartBeat() {
    for (VnfmManagerEndpoint endpoint : vnfmEndpointRepository.findAll()) {
      if (endpoint.getEndpointType().ordinal() == EndpointType.RABBIT.ordinal()) {
        if (endpoint.isEnabled()) {
          try {
            if (!RabbitManager.getQueues(brokerIp.trim(), username, password, managementPort)
                .contains("nfvo." + endpoint.getType() + ".actions")) {
              if (endpoint.isActive()) {
                log.info("Set endpoint " + endpoint.getType() + " to unactive");
                endpoint.setActive(false);
                vnfmEndpointRepository.save(endpoint);
              }
            } else {
              if (!endpoint.isActive()) {
                log.info("Set endpoint " + endpoint.getType() + " to active");
                endpoint.setActive(true);
                vnfmEndpointRepository.save(endpoint);
              }
            }
          } catch (IOException ignored) {
            log.warn("Not able to list queues, probably " + brokerIp.trim() + " is not reachable.");
          }
        }
      }
    }
  }
}
