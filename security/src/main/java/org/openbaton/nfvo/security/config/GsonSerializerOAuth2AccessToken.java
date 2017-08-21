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

package org.openbaton.nfvo.security.config;

import com.google.gson.*;
import java.lang.reflect.Type;
import org.openbaton.catalogue.nfvo.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

@Service
public class GsonSerializerOAuth2AccessToken implements JsonSerializer<OAuth2AccessToken> {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public JsonElement serialize(
      OAuth2AccessToken src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("token", new JsonPrimitive(src.getValue()));
    jsonObject.add("value", new JsonPrimitive(src.getValue()));
    jsonObject.add("access_token", new JsonPrimitive(src.getValue()));
    return jsonObject;
  }
}
