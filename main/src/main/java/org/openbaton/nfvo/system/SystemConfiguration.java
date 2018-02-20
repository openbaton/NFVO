package org.openbaton.nfvo.system;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@EnableRabbit
public class SystemConfiguration {
  @Value("${nfvo.https:false}")
  private boolean https;

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
