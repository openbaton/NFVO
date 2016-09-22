package org.openbaton.nfvo.system;

import org.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.openbaton.vnfm.interfaces.manager.VnfmReceiver;
import org.openbaton.vnfm.interfaces.register.VnfmRegister;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lto on 09/11/15.
 */
@Configuration
@EnableRabbit
@ConfigurationProperties(prefix = "nfvo.rabbit")
public class RabbitConfiguration {
  final static String queueName_vnfmRegister = "nfvo.vnfm.register";
  final static String queueName_vnfmUnregister = "nfvo.vnfm.unregister";
  final static String queueName_vnfmCoreActions = "vnfm.nfvo.actions";
  final static String queueName_vnfmCoreActionsReply = "vnfm.nfvo.actions.reply";
  final static String queueName_eventRegister = "nfvo.event.register";
  final static String queueName_eventUnregister = "nfvo.event.unregister";

  @Value("${nfvo.rabbitmq.autodelete:true}")
  private boolean autodelete;

  @Value("${nfvo.rabbitmq.durable:true}")
  private boolean durable;

  @Value("${nfvo.rabbitmq.exclusive:false}")
  private boolean exclusive;

  @Value("${spring.rabbitmq.listener.concurrency:5}")
  private int minConcurrency;

  @Value("${spring.rabbitmq.listener.max-concurrency:15}")
  private int maxConcurrency;

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

  @Bean
  Queue queue_eventRegister() {
    return new Queue(queueName_eventRegister, durable, exclusive, autodelete);
  }

  @Bean
  Queue queue_eventUnregister() {
    return new Queue(queueName_eventUnregister, durable, exclusive, autodelete);
  }

  @Bean
  Queue queue_vnfmUnregister() {
    return new Queue(queueName_vnfmUnregister, true, exclusive, autodelete);
  }

  @Bean
  Queue queue_vnfmRegister() {
    try {
      return new Queue(queueName_vnfmRegister, true, exclusive, autodelete);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Bean
  Queue queue_vnfmCoreActions() {
    return new Queue(queueName_vnfmCoreActions, durable, exclusive, autodelete);
  }

  @Bean
  Queue queue_vnfmCoreActionsReply() {
    return new Queue(queueName_vnfmCoreActionsReply, durable, exclusive, autodelete);
  }

  @Bean
  TopicExchange exchange() {
    return new TopicExchange("openbaton-exchange");
  }

  @Bean
  Binding binding_eventRegister(TopicExchange exchange) {
    return BindingBuilder.bind(queue_eventRegister()).to(exchange).with(queueName_eventRegister);
  }

  @Bean
  Binding binding_vnfmRegister(TopicExchange exchange) {
    return BindingBuilder.bind(queue_vnfmRegister()).to(exchange).with(queueName_vnfmRegister);
  }

  @Bean
  Binding binding_vnfmUnregister(TopicExchange exchange) {
    return BindingBuilder.bind(queue_vnfmUnregister()).to(exchange).with(queueName_vnfmUnregister);
  }

  @Bean
  Binding binding_vnfmCoreAction(TopicExchange exchange) {
    return BindingBuilder.bind(queue_vnfmCoreActions())
        .to(exchange)
        .with(queueName_vnfmCoreActions);
  }

  @Bean
  Binding binding_vnfmCoreActionReply(TopicExchange exchange) {
    return BindingBuilder.bind(queue_vnfmCoreActionsReply())
        .to(exchange)
        .with(queueName_vnfmCoreActionsReply);
  }

  @Bean
  Binding binding_eventUnregister(TopicExchange exchange) {
    return BindingBuilder.bind(queue_eventUnregister())
        .to(exchange)
        .with(queueName_eventUnregister);
  }

  @Bean
  MessageListenerAdapter listenerAdapter_eventRegister(EventDispatcher eventDispatcher) {
    return new MessageListenerAdapter(eventDispatcher, "register");
  }

  @Bean
  MessageListenerAdapter listenerAdapter_eventUnregister(EventDispatcher eventDispatcher) {
    return new MessageListenerAdapter(eventDispatcher, "unregister");
  }

  @Bean
  MessageListenerAdapter listenerAdapter_vnfmRegister(
      @Qualifier("rabbitRegister") VnfmRegister rabbitRegister) {
    return new MessageListenerAdapter(rabbitRegister, "addManagerEndpoint");
  }

  @Bean
  MessageListenerAdapter listenerAdapter_vnfmUnregister(
      @Qualifier("rabbitRegister") VnfmRegister rabbitUnregister) {
    return new MessageListenerAdapter(rabbitUnregister, "removeManagerEndpoint");
  }

  @Bean
  MessageListenerAdapter listenerAdapter_vnfmCoreActions(
      @Qualifier("rabbitVnfmReceiver") VnfmReceiver vnfmReceiver) {
    return new MessageListenerAdapter(vnfmReceiver, "actionFinishedVoid");
  }

  @Bean
  MessageListenerAdapter listenerAdapter_vnfmCoreActionsReply(
      @Qualifier("rabbitVnfmReceiver") VnfmReceiver vnfmReceiver) {
    return new MessageListenerAdapter(vnfmReceiver, "actionFinished");
  }

  @Bean
  SimpleMessageListenerContainer container_eventRegister(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_eventRegister") MessageListenerAdapter listenerAdapter) {
    return getSimpleMessageListenerContainer(
        connectionFactory, listenerAdapter, queueName_eventRegister);
  }

  private SimpleMessageListenerContainer getSimpleMessageListenerContainer(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_eventRegister") MessageListenerAdapter listenerAdapter,
      String queueName_eventRegister) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(queueName_eventRegister);
    container.setMessageListener(listenerAdapter);
    return container;
  }

  @Bean
  SimpleMessageListenerContainer container_eventUnregister(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_eventUnregister") MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container =
        getSimpleMessageListenerContainer(
            connectionFactory, listenerAdapter, queueName_eventUnregister);
    return container;
  }

  @Bean
  SimpleMessageListenerContainer container_vnfmRegister(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_vnfmRegister") MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container =
        getSimpleMessageListenerContainer(
            connectionFactory, listenerAdapter, queueName_vnfmRegister);
    return container;
  }

  @Bean
  SimpleMessageListenerContainer container_vnfmUnregister(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_vnfmUnregister") MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container =
        getSimpleMessageListenerContainer(
            connectionFactory, listenerAdapter, queueName_vnfmUnregister);
    return container;
  }

  @Bean
  SimpleMessageListenerContainer container_vnfmCoreActions(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_vnfmCoreActions") MessageListenerAdapter listenerAdapter) {
    return getSimpleMessageListenerContainer(
        connectionFactory,
        listenerAdapter,
        queueName_vnfmCoreActions,
        minConcurrency,
        maxConcurrency);
  }

  private SimpleMessageListenerContainer getSimpleMessageListenerContainer(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_vnfmCoreActions") MessageListenerAdapter listenerAdapter,
      String queueName_vnfmCoreActions,
      int minConcurrency,
      int maxConcurrency) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(queueName_vnfmCoreActions);
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

  @Bean
  SimpleMessageListenerContainer container_vnfmCoreActionsReply(
      ConnectionFactory connectionFactory,
      @Qualifier("listenerAdapter_vnfmCoreActionsReply") MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container =
        getSimpleMessageListenerContainer(
            connectionFactory,
            listenerAdapter,
            queueName_vnfmCoreActionsReply,
            minConcurrency,
            maxConcurrency);
    return container;
  }
}
