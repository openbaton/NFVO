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
import java.io.*;
import java.util.concurrent.*;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.openbaton.registration.Registration;
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

  @Value("${spring.rabbitmq.port}")
  private int rabbitPort;

  @Value("${spring.rabbitmq.username}")
  private String rabbitUsername;

  @Value("${spring.rabbitmq.password}")
  private String rabbitPassword;

  @Value("${spring.rabbitmq.virtualHost:/}")
  private String virtualHost;

  @Autowired private Gson gson;
  @Autowired private ConfigurableApplicationContext context;
  @Autowired private Registration registration;

  @Override
  protected void setup() {
    vnfmHelper = (VnfmHelper) context.getBean("vnfmSpringHelperRabbit");
    super.setup();
  }

  @PostConstruct
  private void listenOnQueues() {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(rabbitHost);
    connectionFactory.setPort(rabbitPort);
    connectionFactory.setUsername(rabbitUsername);
    connectionFactory.setPassword(rabbitPassword);
    connectionFactory.setVirtualHost(virtualHost);
    try {
      Connection connection = connectionFactory.newConnection();
      final Channel channel = connection.createChannel();
      channel.basicConsume(
          getEndpoint(),
          false,
          new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(
                String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                throws IOException {
              ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
              String incomingString = getStringFromInputStream(byteArrayInputStream);
              NFVMessage nfvMessage = gson.fromJson(incomingString, NFVMessage.class);
              NFVMessage answerMessage = null;
              try {
                answerMessage = onAction(nfvMessage);
              } catch (NotFoundException e) {
                log.error("Error while processing message from NFVO");
                e.printStackTrace();
              } catch (BadFormatException e) {
                log.error("Error while processing message from NFVO");
                e.printStackTrace();
              }
              if (answerMessage != null) {
                AMQP.BasicProperties replyProps =
                    new AMQP.BasicProperties.Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();
                try {
                  String answer = gson.toJson(answerMessage, NFVMessage.class);
                  channel.basicPublish(
                      "", properties.getReplyTo(), replyProps, answer.getBytes("UTF-8"));
                  channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
          });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void unregister() {
    try {
      // ((VnfmSpringHelperRabbit) vnfmHelper)
      //     .sendMessageToQueue(RabbitConfiguration.queueName_vnfmUnregister, vnfmManagerEndpoint);
      registration.deregisterVnfmFromNfvo(
          ((VnfmSpringHelperRabbit) vnfmHelper).getRabbitTemplate(), vnfmManagerEndpoint);
      ((VnfmSpringHelperRabbit) vnfmHelper)
          .deleteQueue(
              properties.getProperty("endpoint"),
              brokerIp,
              Integer.parseInt(brokerPort),
              rabbitUsername,
              rabbitPassword);

    } catch (IllegalStateException e) {
      log.error("Got exception while deregistering the VNFM from the NFVO");
    } catch (TimeoutException e) {
      log.error("Got exception while deregistering the VNFM from the NFVO");
    } catch (IOException e) {
      log.error("Got exception while deregistering the VNFM from the NFVO");
    }
  }

  @Override
  protected void register() {
    String[] usernamePassword =
        registration.registerVnfmToNfvo(
            ((VnfmSpringHelperRabbit) vnfmHelper).getRabbitTemplate(), vnfmManagerEndpoint);

    this.rabbitUsername = usernamePassword[0];
    this.rabbitPassword = usernamePassword[1];

    try {
      ((VnfmSpringHelperRabbit) vnfmHelper)
          .createQueue(
              rabbitHost,
              rabbitPort,
              rabbitUsername,
              rabbitPassword,
              virtualHost,
              properties.getProperty("endpoint"),
              "openbaton-exchange");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }

    log.info("Correctly registered to NFVO");
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    unregister();
  }

  private static String getStringFromInputStream(InputStream is) {

    BufferedReader br = null;
    StringBuilder sb = new StringBuilder();

    String line;
    try {

      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return sb.toString();
  }
}
