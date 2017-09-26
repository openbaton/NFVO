package org.openbaton.nfvo.api.configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;

@Configuration
public class WebInitializer implements WebApplicationInitializer {

  @Autowired private CustomDispatcherServlet customDispatcherServlet;

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {

    servletContext.addServlet("dispatchServlet", customDispatcherServlet);
  }
}
