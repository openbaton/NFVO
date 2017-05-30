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

  private boolean exit;

  @Override
  protected void setup() {
    vnfmHelper = (VnfmHelper) context.getBean("vnfmSpringHelperRabbit");
    super.setup();
  }

  @PostConstruct
  private void listenOnQueues() {
    // start listening on queues
    final ExecutorService pool = Executors.newFixedThreadPool(1);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                pool.shutdownNow();
                exit = true;
              }
            });

    RabbitNfvMessageListener messageListener =
        new RabbitNfvMessageListener(
            getEndpoint(), rabbitHost, rabbitPort, rabbitUsername, rabbitPassword, virtualHost);

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");

    while (!exit) {
      Future<String> futureMessage = pool.submit(messageListener);
      try {
        String nfvMessageString = futureMessage.get();
        if (nfvMessageString != null && !nfvMessageString.equals("")) {
          NFVMessage nfvMessage = gson.fromJson(nfvMessageString, NFVMessage.class);
          NFVMessage answer = this.onAction(nfvMessage);
          if (answer != null) {
            messageListener.rpcReply(answer);
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (BadFormatException e) {
        e.printStackTrace();
      } catch (NotFoundException e) {
        e.printStackTrace();
      }
    }
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
    // ((VnfmSpringHelperRabbit) vnfmHelper)
    //    .sendMessageToQueue(RabbitConfiguration.queueName_vnfmRegister, vnfmManagerEndpoint);

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
}
