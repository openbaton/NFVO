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

package org.openbaton.common.vnfm_sdk.amqp.configuration;

import java.io.IOException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.openbaton.common.vnfm_sdk.amqp.AbstractVnfmSpringAmqp;
import org.openbaton.common.vnfm_sdk.interfaces.LogDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/** Created by lto on 09/11/15. */
@Configuration
@EnableRabbit
@ConfigurationProperties(prefix = "vnfm.rabbitmq")
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class RabbitConfiguration {
  public static final String queueName_vnfmRegister = "nfvo.vnfm.register";
  public static final String queueName_vnfmUnregister = "nfvo.vnfm.unregister";
  public static final String queueName_vnfmCoreActions = "vnfm.nfvo.actions";
  public static final String queueName_vnfmCoreActionsReply = "vnfm.nfvo.actions.reply";
  public static String queueName_nfvoGenericActions = "nfvo.type.actions";
  private static final String queueName_logDispatch = "nfvo.vnfm.logs";

  private RabbitAdmin rabbitAdmin;

  private boolean autodelete = true;
  private boolean durable;
  private boolean exclusive;
  private int minConcurrency;
  private int maxConcurrency;

  @Autowired private ConnectionFactory connectionFactory;

  @Autowired(required = false)
  private LogDispatcher logDispatcher;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  public int getMaxConcurrency() {
    return maxConcurrency;
  }

  public void setMaxConcurrency(int maxConcurrency) {
    this.maxConcurrency = maxConcurrency;
  }

  public int getMinConcurrency() {
    return minConcurrency;
  }

  public void setMinConcurrency(int minConcurrency) {
    this.minConcurrency = minConcurrency;
  }

  public boolean isAutodelete() {
    return autodelete;
  }

  public void setAutodelete(boolean autodelete) {
    this.autodelete = autodelete;
  }

  public boolean isDurable() {
    return durable;
  }

  public void setDurable(boolean durable) {
    this.durable = durable;
  }

  public boolean isExclusive() {
    return exclusive;
  }

  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }

  @PostConstruct
  private void init() {
    log.info("Initialization of RabbitConfiguration");
    rabbitAdmin = new RabbitAdmin(connectionFactory);
    TopicExchange topicExchange = new TopicExchange("openbaton-exchange");
    rabbitAdmin.declareExchange(topicExchange);
    log.info("exchange declared");
  }

  @Bean
  Queue queue_logDispatch() {
    return new Queue(queueName_logDispatch, durable, exclusive, autodelete);
  }

  @Bean
  Binding binding_logDispatch(TopicExchange exchange) {
    return BindingBuilder.bind(queue_logDispatch()).to(exchange).with(queueName_logDispatch);
  }

  @Bean
  MessageListenerAdapter listenerAdapter_logDispatch() {
    if (logDispatcher != null) return new MessageListenerAdapter(logDispatcher, "sendLogs");
    else return null;
  }

  @Bean
  SimpleMessageListenerContainer container_logDispatcher(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_logDispatch") MessageListenerAdapter listenerAdapter) {
    if (listenerAdapter != null) {
      SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
      container.setConnectionFactory(connectionFactory);
      container.setQueueNames(queueName_logDispatch);
      if (minConcurrency <= 0 || maxConcurrency <= 0 || minConcurrency > maxConcurrency) {
        container.setConcurrentConsumers(5);
        container.setMaxConcurrentConsumers(15);
      } else {
        container.setConcurrentConsumers(minConcurrency);
        container.setMaxConcurrentConsumers(maxConcurrency);
      }
      container.setMessageListener(listenerAdapter);
      return container;
    } else return null;
  }

  @Bean
  Queue queue_genericVnfmActions() {
    Properties properties = new Properties();

    try {
      properties.load(ClassLoader.getSystemResourceAsStream("conf.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    queueName_nfvoGenericActions = "nfvo." + properties.getProperty("type") + ".actions";
    return new Queue(queueName_nfvoGenericActions, durable, exclusive, autodelete);
  }

  @Bean
  TopicExchange exchange() {
    TopicExchange topicExchange = new TopicExchange("openbaton-exchange");
    return topicExchange;
  }

  @Bean
  Binding binding_nfvoGenericAction(TopicExchange exchange) {
    return BindingBuilder.bind(queue_genericVnfmActions())
        .to(exchange)
        .with(queueName_nfvoGenericActions);
  }

  @Bean
  MessageListenerAdapter listenerAdapter_nfvoGenericActions(AbstractVnfmSpringAmqp receiver) {
    return new MessageListenerAdapter(receiver, "onAction");
  }

  @Bean
  SimpleMessageListenerContainer container_eventRegister(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_nfvoGenericActions") MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(queueName_nfvoGenericActions);
    if (minConcurrency <= 0 || maxConcurrency <= 0 || minConcurrency > maxConcurrency) {
      container.setConcurrentConsumers(5);
      container.setMaxConcurrentConsumers(15);
    } else {
      container.setConcurrentConsumers(minConcurrency);
      container.setMaxConcurrentConsumers(maxConcurrency);
    }
    container.setMessageListener(listenerAdapter);
    return container;
  }
}
