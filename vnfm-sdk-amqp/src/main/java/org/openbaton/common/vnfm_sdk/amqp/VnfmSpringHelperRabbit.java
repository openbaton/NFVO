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

package org.openbaton.common.vnfm_sdk.amqp;

import com.google.gson.Gson;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.openbaton.common.vnfm_sdk.amqp.configuration.RabbitConfiguration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by lto on 23/09/15.
 */
@Service
@Scope
@ConfigurationProperties
public class VnfmSpringHelperRabbit extends VnfmHelper {

    @Autowired
    private Gson gson;

    @Value("${vnfm.rabbitmq.autodelete}")
    private boolean autodelete = true;
    @Value("${vnfm.rabbitmq.durable}")
    private boolean durable;
    @Value("${vnfm.rabbitmq.exclusive}")
    private boolean exclusive;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private RabbitAdmin rabbitAdmin;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("${vnfm.rabbitmq.sar.timeout:1000}")
    private int timeout;

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutodelete() {
        return autodelete;
    }

    public void setAutodelete(boolean autodelete) {
        this.autodelete = autodelete;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @PostConstruct
    private void init() throws IOException {
        log.info("Initialization of VnfmSpringHelperRabbit");
        rabbitAdmin = new RabbitAdmin(connectionFactory);
    }

    public void sendMessageToQueue(String sendToQueueName, final Serializable message) {
        log.debug("Sending message to Queue:  " + sendToQueueName);

        if (sendToQueueName.equals(RabbitConfiguration.queueName_vnfmRegister)) {
            rabbitAdmin.declareQueue(new Queue(sendToQueueName, true, exclusive, autodelete));
            rabbitAdmin.declareBinding(new Binding(sendToQueueName, Binding.DestinationType.QUEUE, "openbaton-exchange", sendToQueueName, null));
        }

        rabbitTemplate.convertAndSend(sendToQueueName, gson.toJson(message));
    }


    @Override
    public void sendToNfvo(final NFVMessage nfvMessage) {
        sendMessageToQueue(RabbitConfiguration.queueName_vnfmCoreActions, nfvMessage);
    }

    @Override
    public NFVMessage sendAndReceive(NFVMessage message) throws Exception {

        rabbitTemplate.setReplyTimeout(timeout * 1000);
        rabbitTemplate.afterPropertiesSet();
        String response = (String) this.rabbitTemplate.convertSendAndReceive(RabbitConfiguration.queueName_vnfmCoreActionsReply, gson.toJson(message));

        return gson.fromJson(response, NFVMessage.class);
    }

    @Override
    public String sendAndReceive(String message, String queueName) throws Exception {

        rabbitTemplate.setReplyTimeout(timeout * 1000);
        rabbitTemplate.afterPropertiesSet();

        log.debug("Sending to: " + queueName);
        String res = (String) rabbitTemplate.convertSendAndReceive("openbaton-exchange", queueName, message);
        log.trace("Received from EMS: " + res);
        return res;
    }
}
