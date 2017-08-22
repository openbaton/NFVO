package org.openbaton.nfvo.api.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.DispatcherServlet;

@Service
public class CustomDispatcherServlet extends DispatcherServlet {

  @Override
  protected void doDispatch(HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    CustomHttpServletRequestWrapper wrappedRequest = new CustomHttpServletRequestWrapper(request);

    super.doDispatch(wrappedRequest, response);
  }
}
