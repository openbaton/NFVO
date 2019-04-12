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

package org.openbaton.nfvo.security.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.nfvo.common.configuration.NfvoGsonDeserializerNFVMessage;
import org.openbaton.nfvo.common.configuration.NfvoGsonDeserializerVimInstance;
import org.openbaton.nfvo.common.configuration.NfvoGsonSerializerVimInstance;
import org.openbaton.nfvo.security.authentication.GsonSerializerOAuth2AccessToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Configuration
public class SecurityConfigurationGson {

  @Bean
  @Scope("prototype")
  Gson gson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(NFVMessage.class, new NfvoGsonDeserializerNFVMessage())
        .registerTypeAdapter(BaseVimInstance.class, new NfvoGsonDeserializerVimInstance())
        .registerTypeAdapter(BaseVimInstance.class, new NfvoGsonSerializerVimInstance())
        .registerTypeAdapter(OAuth2AccessToken.class, new GsonSerializerOAuth2AccessToken())
        .create();
  }
}
