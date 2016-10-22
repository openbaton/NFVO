package org.openbaton.common.vnfm_sdk.rest.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by tbr on 21.10.16.
 */
@Configuration
public class GsonConfiguration {

  @Bean
  Gson gson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage())
        .create();
  }
}
