package org.openbaton.nfvo.security.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.nfvo.common.configuration.NfvoGsonDeserializerNFVMessage;
import org.openbaton.nfvo.common.configuration.NfvoGsonDeserializerVimInstance;
import org.openbaton.nfvo.common.configuration.NfvoGsonSerializerVimInstance;
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
