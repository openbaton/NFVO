package org.openbaton.nfvo.common.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by lto on 10/11/15.
 */
@Configuration
public class SystemConfiguration {

    @Bean
    @Scope("prototype")
    Gson gson(){
        return new GsonBuilder().setPrettyPrinting().registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage()).create();
    }
}
