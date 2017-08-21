package org.openbaton.nfvo.security.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.nfvo.common.configuration.GsonDeserializerNFVMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SecurityConfigurationGson {

  @Bean
  @Scope("prototype")
  Gson gson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage())
        //.registerTypeAdapter(OAuth2AccessToken.class, new GsonSerializerOAuth2AccessToken())
        .create();
  }
}
