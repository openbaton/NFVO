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
import java.lang.reflect.Type;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.GenericVimInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NfvoGsonDeserializerVimInstance implements JsonDeserializer<BaseVimInstance> {

  private Gson gson =
      new GsonBuilder()
          //          .registerTypeAdapter(BaseNetwork.class, new NfvoGsonDeserializerNetwork())
          //          .registerTypeAdapter(BaseNfvImage.class, new NfvoGsonDeserializerImage())
          .setPrettyPrinting()
          .create();

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public BaseVimInstance deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String type = json.getAsJsonObject().get("type").getAsString();
    BaseVimInstance result;
    try {
      String className =
          "org.openbaton.catalogue.nfvo.viminstances."
              + type.substring(0, 1).toUpperCase()
              + type.substring(1)
              + "VimInstance";
      log.debug("Looking for class " + className);
      Class<? extends BaseVimInstance> clz =
          (Class<? extends BaseVimInstance>) Class.forName(className);
      result = gson.fromJson(json, clz);
    } catch (ClassNotFoundException e) {
      result = gson.fromJson(json, GenericVimInstance.class);
    }

    log.trace("Deserialized message is " + result);
    return result;
  }
}
