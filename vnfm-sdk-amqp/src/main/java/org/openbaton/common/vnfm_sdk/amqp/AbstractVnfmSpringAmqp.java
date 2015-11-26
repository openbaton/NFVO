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
import org.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.openbaton.common.vnfm_sdk.amqp.configuration.RabbitConfiguration;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by lto on 28/05/15.
 */

@SpringBootApplication
@ComponentScan(basePackages = "org.openbaton")
public abstract class AbstractVnfmSpringAmqp extends AbstractVnfm {

    @Autowired
    private Gson gson;
    @Autowired
    private ConfigurableApplicationContext context;

    public void onAction(String message) throws NotFoundException, BadFormatException {
        //TODO deserialize message

        NFVMessage nfvMessage = gson.fromJson(message, NFVMessage.class);

        this.onAction(nfvMessage);
    }

    @Override
    protected void setup() {
        vnfmHelper = (VnfmHelper) context.getBean("vnfmSpringHelperRabbit");
        super.setup();
    }

    protected abstract void checkEmsStarted(String hostname) throws RuntimeException;

    @Override
    protected void unregister() {
        ((VnfmSpringHelperRabbit) vnfmHelper).sendMessageToQueue(RabbitConfiguration.queueName_vnfmUnregister, vnfmManagerEndpoint);
    }

    @Override
    protected void register() {
        ((VnfmSpringHelperRabbit) vnfmHelper).sendMessageToQueue(RabbitConfiguration.queueName_vnfmRegister, vnfmManagerEndpoint);
    }
}

