package org.openbaton.common.vnfm_sdk.amqp.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.common.vnfm_sdk.amqp.AbstractVnfmSpringAmqp;
import org.openbaton.common.vnfm_sdk.interfaces.EmsRegistrator;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lto on 09/11/15.
 */
@Configuration
@EnableRabbit
@ConfigurationProperties(prefix = "vnfm.rabbitmq")
public class RabbitConfiguration {
    public final static String queueName_vnfmRegister = "nfvo.vnfm.register";
    public final static String queueName_vnfmUnregister = "nfvo.vnfm.unregister";
    public final static String queueName_vnfmCoreActions = "vnfm.nfvo.actions";
    public final static String queueName_vnfmCoreActionsReply = "vnfm.nfvo.actions.reply";
    public final static String queueName_nfvoGenericActions = "nfvo.generic.actions";
    public final static String queueName_emsRegistrator = "ems.generic.register";

    private boolean autodelete;
    private boolean durable;
    private boolean exclusive;
    private int minConcurrency;
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
    Gson gson() {
        return new GsonBuilder().setPrettyPrinting().registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage()).create();
    }

    @Bean
    Queue queue_genericVnfmActions() {
        return new Queue(queueName_nfvoGenericActions, durable, exclusive, autodelete);
    }

    @Bean
    Queue queue_emsRegistrator() {
        return new Queue(queueName_emsRegistrator, durable, exclusive, autodelete);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("openbaton-exchange");
    }

    @Bean
    Binding binding_vnfmCoreActionReply(TopicExchange exchange) {
        return BindingBuilder.bind(queue_emsRegistrator()).to(exchange).with(queueName_emsRegistrator);
    }

    @Bean
    Binding binding_nfvoGenericAction(TopicExchange exchange) {
        return BindingBuilder.bind(queue_genericVnfmActions()).to(exchange).with(queueName_nfvoGenericActions);
    }

    @Bean
    MessageListenerAdapter listenerAdapter_nfvoGenericActions(AbstractVnfmSpringAmqp receiver) {
        return new MessageListenerAdapter(receiver, "onAction");
    }

    @Bean
    MessageListenerAdapter listenerAdapter_emsRegistrator(EmsRegistrator receiver) {
        return new MessageListenerAdapter(receiver, "register");
    }

    @Bean
    SimpleMessageListenerContainer container_eventRegister(ConnectionFactory connectionFactory, @Qualifier("listenerAdapter_nfvoGenericActions") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName_nfvoGenericActions);
        if (minConcurrency <= 0 || maxConcurrency <= 0 || minConcurrency > maxConcurrency) {
            container.setConcurrentConsumers(5);
            container.setMaxConcurrentConsumers(15);
        }else {
            container.setConcurrentConsumers(minConcurrency);
            container.setMaxConcurrentConsumers(maxConcurrency);
        }
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer container_emsRegistrator(ConnectionFactory connectionFactory, @Qualifier("listenerAdapter_emsRegistrator") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName_emsRegistrator);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(15);
        container.setMessageListener(listenerAdapter);
        return container;
    }
}
