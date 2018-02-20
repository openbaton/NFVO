package org.openbaton.nfvo.api.configuration;

import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;

@Configuration
public class WebInitializer implements WebApplicationInitializer {

  @Autowired private CustomDispatcherServlet customDispatcherServlet;

  @Override
  public void onStartup(ServletContext servletContext) {

    servletContext.addServlet("dispatchServlet", customDispatcherServlet);
  }
}
