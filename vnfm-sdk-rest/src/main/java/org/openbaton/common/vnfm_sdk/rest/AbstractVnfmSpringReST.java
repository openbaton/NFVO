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

package org.openbaton.common.vnfm_sdk.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.openbaton.common.vnfm_sdk.rest.configuration.GsonDeserializerNFVMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

//import javax.validation.Valid;

/**
 * Created by lto on 08/07/15.
 */
@SpringBootApplication
@RestController

public abstract class AbstractVnfmSpringReST extends AbstractVnfm {

    private VnfmRestHelper vnfmRestHelper;
    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private Gson gson;

    @Bean
    Gson gson() {
        return new GsonBuilder().setPrettyPrinting().registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage()).create();
    }


    @Override
    protected void setup() {
        this.vnfmRestHelper = (VnfmRestHelper) context.getBean("vnfmRestHelper");
        this.vnfmHelper = vnfmRestHelper;
        super.setup();
    }

    @Override
    protected void unregister() {
        vnfmRestHelper.unregister(vnfmManagerEndpoint);
    }

    @Override
    protected void register() {
        vnfmRestHelper.register(vnfmManagerEndpoint);
    }

    @RequestMapping(value = "/core-rest-actions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@RequestBody /*@Valid*/ String jsonNfvMessage) {
        log.debug("Received: " + jsonNfvMessage);

        NFVMessage nfvMessage = gson.fromJson(jsonNfvMessage, NFVMessage.class);

        try {
            this.onAction(nfvMessage);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (BadFormatException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
