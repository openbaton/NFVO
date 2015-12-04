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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
@ConfigurationProperties(prefix = "vnfm.rabbitmq.sar")
public class VnfmSpringHelperRabbit extends VnfmHelper {

    @Autowired
    private Gson gson;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private int timeout;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @PostConstruct
    private void init() throws IOException {
        log.info("Initialization of VnfmSpringHelperRabbit");
    }

    public void sendMessageToQueue(String sendToQueueName, final Serializable message) {
        log.debug("Sending message to Queue:  " + sendToQueueName);

        rabbitTemplate.convertAndSend(sendToQueueName, gson.toJson(message));
    }


    /**
     * This method should be used for receiving text message from EMS
     * <p/>
     * resp = {
     * 'output': out,          // the output of the command
     * 'err': err,             // the error outputs of the commands
     * 'status': status        // the exit status of the command
     * }
     *
     * @param queueName
     * @return
     */
    public String receiveTextFromQueue(String queueName) {
        String res = null;

        return res;
    }

    @Override
    public void sendToNfvo(final NFVMessage nfvMessage) {
        sendMessageToQueue(RabbitConfiguration.queueName_vnfmCoreActions, nfvMessage);
    }

    @Override
    public NFVMessage sendAndReceive(NFVMessage message) throws Exception {
        if (timeout == 0)
            timeout = 1000;
        rabbitTemplate.setReplyTimeout(timeout * 1000);
        rabbitTemplate.afterPropertiesSet();
        String response = (String) this.rabbitTemplate.convertSendAndReceive(RabbitConfiguration.queueName_vnfmCoreActionsReply, gson.toJson(message));

        return gson.fromJson(response, NFVMessage.class);
    }

    @Override
    public String sendAndReceive(String message, String queueName) throws Exception {
        if (timeout == 0)
            timeout = 1000;
        rabbitTemplate.setReplyTimeout(timeout * 1000);
        rabbitTemplate.afterPropertiesSet();

        log.debug("Sending to: " + queueName);
        String res = (String) rabbitTemplate.convertSendAndReceive("openbaton-exchange",queueName, message);
        log.debug("Received from EMS: " + res);
        return res;
    }
}
