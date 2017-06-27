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
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;
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

  @Value("${vnfm.consumers.num:5}")
  private int consumers;

  @Autowired private Gson gson;
  @Autowired private ConfigurableApplicationContext context;
  @Autowired private Registration registration;

  @Override
  protected void setup() {
    vnfmHelper = (VnfmHelper) context.getBean("vnfmSpringHelperRabbit");
    super.setup();
  }

  private class ConsumerRunnable implements Runnable {
    @Override
    public void run() {
      ConnectionFactory connectionFactory = new ConnectionFactory();
      connectionFactory.setHost(rabbitHost);
      connectionFactory.setPort(rabbitPort);
      connectionFactory.setUsername(rabbitUsername);
      connectionFactory.setPassword(rabbitPassword);
      connectionFactory.setVirtualHost(virtualHost);
      Connection connection = null;
      try {
        connection = connectionFactory.newConnection();
        final Channel channel = connection.createChannel();
        channel.basicQos(1);
        DefaultConsumer consumer =
            new DefaultConsumer(channel) {

              @Override
              public void handleDelivery(
                  String consumerTag,
                  Envelope envelope,
                  AMQP.BasicProperties properties,
                  byte[] body)
                  throws IOException {
                AMQP.BasicProperties replyProps =
                    new AMQP.BasicProperties.Builder()
                        .correlationId(properties.getCorrelationId())
                        .contentType("plain/text")
                        .build();

                NFVMessage answerMessage = null;
                try {
                  NFVMessage nfvMessage =
                      gson.fromJson(
                          getStringFromInputStream(new ByteArrayInputStream(body)),
                          NFVMessage.class);

                  answerMessage = onAction(nfvMessage);
                } catch (NotFoundException e) {
                  log.error("Error while processing message from NFVO");
                  e.printStackTrace();
                } catch (BadFormatException e) {
                  log.error("Error while processing message from NFVO");
                  e.printStackTrace();
                } finally {
                  String answer = gson.toJson(answerMessage);
                  channel.basicPublish(
                      "", properties.getReplyTo(), replyProps, answer.getBytes("UTF-8"));
                  channel.basicAck(envelope.getDeliveryTag(), false);
                }
              }
            };
        channel.basicConsume(getEndpoint(), false, consumer);

        //loop to prevent reaching finally block
        while (true) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException _ignore) {
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (TimeoutException e) {
        e.printStackTrace();
      } finally {
        if (connection != null) {
          try {
            connection.close();
          } catch (IOException _ignore) {
          }
        }
      }
    }
  }

  @PostConstruct
  private void listenOnQueues() {

    for (int i = 0; i < consumers; i++) {
      Runnable listenerRunnable = new ConsumerRunnable();
      Thread thread = new Thread(listenerRunnable);
      thread.setDaemon(true);
      thread.start();
    }
    log.info("Started " + consumers + " consumers");
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
              rabbitHost,
              rabbitPort,
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
    String[] usernamePassword = new String[0];
    try {
      usernamePassword =
          registration.registerVnfmToNfvo(
              ((VnfmSpringHelperRabbit) vnfmHelper).getRabbitTemplate(), vnfmManagerEndpoint);
    } catch (InterruptedException e) {
      e.printStackTrace();
      log.error("Not able to register..");
      System.exit(2);
    }

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
