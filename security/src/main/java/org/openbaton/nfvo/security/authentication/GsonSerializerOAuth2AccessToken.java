/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.security.authentication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Set;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class GsonSerializerOAuth2AccessToken implements JsonSerializer<OAuth2AccessToken> {

  @Override
  public JsonElement serialize(
      OAuth2AccessToken src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(OAuth2AccessToken.ACCESS_TOKEN, src.getValue());
    // back compatibility for dashboard
    jsonObject.addProperty("value", src.getValue());

    jsonObject.addProperty(OAuth2AccessToken.TOKEN_TYPE, src.getTokenType());

    OAuth2RefreshToken refreshToken = src.getRefreshToken();
    if (refreshToken != null) {
      jsonObject.addProperty(OAuth2AccessToken.REFRESH_TOKEN, refreshToken.getValue());
    }
    Date expiration = src.getExpiration();
    if (expiration != null) {
      long now = System.currentTimeMillis();
      jsonObject.add(
          OAuth2AccessToken.EXPIRES_IN, new JsonPrimitive((expiration.getTime() - now) / 1000));
    }

    Set<String> scope = src.getScope();

    if (scope != null && !scope.isEmpty()) {
      StringBuilder scopes = new StringBuilder();
      for (String s : scope) {
        Assert.hasLength(s, "Scopes cannot be null or empty. Got " + scope + "");
        scopes.append(s);
        scopes.append(" ");
      }

      jsonObject.addProperty(OAuth2AccessToken.SCOPE, scopes.substring(0, scopes.length() - 1));
    }

    return jsonObject;
  }
}
