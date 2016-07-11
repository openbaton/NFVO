package org.openbaton.nfvo.common.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by lto on 10/11/15.
 */
@Configuration
@ConfigurationProperties
public class SystemConfiguration {

  @Value("${server.https:false}")
  private boolean https;

  @Bean
  @Scope("prototype")
  Gson gson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage())
        .create();
  }

  @Bean
  public EmbeddedServletContainerFactory servletContainer() {
    if (https) {
      TomcatEmbeddedServletContainerFactory tomcat =
          new TomcatEmbeddedServletContainerFactory() {
            @Override
            protected void postProcessContext(Context context) {
              SecurityConstraint securityConstraint = new SecurityConstraint();
              securityConstraint.setUserConstraint("CONFIDENTIAL");
              SecurityCollection collection = new SecurityCollection();
              collection.addPattern("/*");
              securityConstraint.addCollection(collection);
              context.addConstraint(securityConstraint);
            }
          };

      tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
      return tomcat;
    }
    return new TomcatEmbeddedServletContainerFactory();
  }

  private Connector initiateHttpConnector() {

    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");

    connector.setScheme("http");
    connector.setPort(8080);
    if (https) {
      connector.setSecure(false);
      connector.setRedirectPort(8443);
    }
    return connector;
  }
}
