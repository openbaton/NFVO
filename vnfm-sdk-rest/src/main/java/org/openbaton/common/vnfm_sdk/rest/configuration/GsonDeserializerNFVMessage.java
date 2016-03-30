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
package org.openbaton.common.vnfm_sdk.rest.configuration;

import com.google.gson.*;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

/**
 * Created by lto on 10/11/15.
 */
@Service
public class GsonDeserializerNFVMessage implements JsonDeserializer<NFVMessage> {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public NFVMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String action = json.getAsJsonObject().get("action").getAsString();
        NFVMessage result;
        switch (action){
            case "INSTANTIATE":
                result = gson.fromJson(json, OrVnfmInstantiateMessage.class);
                break;
            case "GRANT_OPERATION":
                result = gson.fromJson(json, OrVnfmGrantLifecycleOperationMessage.class);
                break;
            case "SCALING":
                result = gson.fromJson(json, OrVnfmScalingMessage.class);
                break;
            case "SCALE_OUT":
                result = gson.fromJson(json, OrVnfmScalingMessage.class);
                break;
            case "SCALE_IN":
                result = gson.fromJson(json, OrVnfmScalingMessage.class);
                break;
            case "HEAL":
                result = gson.fromJson(json, OrVnfmHealVNFRequestMessage.class);
                break;
            case "ERROR":
                result = gson.fromJson(json, OrVnfmErrorMessage.class);
                break;
            default:
                result = gson.fromJson(json, OrVnfmGenericMessage.class);
                break;
        }
        result.setAction(Action.valueOf(action));
        log.trace("Deserialized message is " + result);
        return result;
    }
}
