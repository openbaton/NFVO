/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Properties;

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
    public static String queueName_nfvoGenericActions = "nfvo.type.actions";
    public static String queueName_emsRegistrator = "ems.generic.register";

    private boolean autodelete = true;
    private boolean durable;
    private boolean exclusive;
    private int minConcurrency;
    private int maxConcurrency;

    @Autowired(required = false)
    private EmsRegistrator registrator;

    @Autowired(required = false)
    @Qualifier("listenerAdapter_emsRegistrator")
    private MessageListenerAdapter listenerAdapter_emsRegistrator;

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
    Queue queue_emsRegistrator() {
        Properties properties = new Properties();

        try {
            properties.load(ClassLoader.getSystemResourceAsStream("conf.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        queueName_emsRegistrator = "ems." + properties.getProperty("type") + ".register";

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
    MessageListenerAdapter listenerAdapter_emsRegistrator() {
        if (registrator != null)
            return new MessageListenerAdapter(registrator, "register");
        else
            return null;
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
    SimpleMessageListenerContainer container_emsRegistrator(ConnectionFactory connectionFactory) {
        if (listenerAdapter_emsRegistrator != null) {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setQueueNames(queueName_emsRegistrator);
            container.setConcurrentConsumers(1);
            container.setMaxConcurrentConsumers(15);
            container.setMessageListener(listenerAdapter_emsRegistrator);
            return container;
        }
        else
            return null;
    }
}
