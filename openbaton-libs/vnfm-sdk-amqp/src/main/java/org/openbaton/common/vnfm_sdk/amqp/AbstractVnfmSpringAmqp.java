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

package org.openbaton.common.vnfm_sdk.amqp;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import java.io.IOException;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.openbaton.common.vnfm_sdk.amqp.configuration.RabbitConfiguration;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;

/** Created by lto on 28/05/15. */
@SpringBootApplication
@ComponentScan(basePackages = "org.openbaton")
@ConfigurationProperties
public abstract class AbstractVnfmSpringAmqp extends AbstractVnfm
    implements ApplicationListener<ContextClosedEvent> {

  @Value("${spring.rabbitmq.host}")
  private String rabbitHost;

  @Value("${spring.rabbitmq.username}")
  private String rabbitUsername;

  @Value("${spring.rabbitmq.password}")
  private String rabbitPassword;

  @Autowired private Gson gson;
  @Autowired private ConfigurableApplicationContext context;

  public void onAction(String message) throws NotFoundException, BadFormatException {

    NFVMessage nfvMessage = gson.fromJson(message, NFVMessage.class);

    this.onAction(nfvMessage);
  }

  @Override
  protected void setup() {
    vnfmHelper = (VnfmHelper) context.getBean("vnfmSpringHelperRabbit");
    super.setup();
  }

  @Override
  protected void unregister() {
    try {
      ((VnfmSpringHelperRabbit) vnfmHelper)
          .sendMessageToQueue(RabbitConfiguration.queueName_vnfmUnregister, vnfmManagerEndpoint);
    } catch (IllegalStateException e) {
      log.warn("Got exception while unregistering trying to do it manually");
      ConnectionFactory factory = new ConnectionFactory();

      factory.setHost(rabbitHost);
      factory.setUsername(rabbitUsername);
      factory.setPassword(rabbitPassword);
      Connection connection = null;
      try {
        connection = factory.newConnection();

        Channel channel = connection.createChannel();

        String message = gson.toJson(vnfmManagerEndpoint);
        channel.basicPublish(
            "openbaton-exchange",
            RabbitConfiguration.queueName_vnfmUnregister,
            MessageProperties.TEXT_PLAIN,
            message.getBytes("UTF-8"));
        log.debug("Sent '" + message + "'");

        channel.close();
        connection.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }

  @Override
  protected void register() {
    ((VnfmSpringHelperRabbit) vnfmHelper)
        .sendMessageToQueue(RabbitConfiguration.queueName_vnfmRegister, vnfmManagerEndpoint);
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    unregister();
  }
}
