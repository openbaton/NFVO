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

package org.openbaton.nfvo.common.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrAllocateResourcesMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrErrorMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrLogMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScaledMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScalingMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrStartStopMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Date;

@Service
public class GsonDeserializerNFVMessage implements JsonDeserializer<NFVMessage> {

  private Gson gson =
      new GsonBuilder()
          .setPrettyPrinting()
          .registerTypeAdapter(Date.class, new GsonDeserializerDate())
          .registerTypeAdapter(Date.class, new GsonSerializerDate())
          .create();

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public NFVMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String action = json.getAsJsonObject().get("action").getAsString();
    NFVMessage result;
    switch (action) {
      case "ALLOCATE_RESOURCES":
        result = gson.fromJson(json, VnfmOrAllocateResourcesMessage.class);
        break;
      case "ERROR":
        result = gson.fromJson(json, VnfmOrErrorMessage.class);
        break;
      case "INSTANTIATE":
        log.trace("gson is: " + gson);
        result = gson.fromJson(json, VnfmOrInstantiateMessage.class);
        break;
      case "SCALED":
        result = gson.fromJson(json, VnfmOrScaledMessage.class);
        break;
      case "SCALING":
        result = gson.fromJson(json, VnfmOrScalingMessage.class);
        break;
      case "HEAL":
        result = gson.fromJson(json, VnfmOrHealedMessage.class);
        break;
      case "START":
        result = gson.fromJson(json, VnfmOrStartStopMessage.class);
        break;
      case "STOP":
        result = gson.fromJson(json, VnfmOrStartStopMessage.class);
        break;
      case "LOG_REQUEST":
        result = gson.fromJson(json, VnfmOrLogMessage.class);
        break;
      default:
        result = gson.fromJson(json, VnfmOrGenericMessage.class);
        break;
    }
    result.setAction(Action.valueOf(action));
    log.trace("Deserialized message is " + result);
    return result;
  }
}
