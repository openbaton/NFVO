package org.openbaton.nfvo.api.interceptors;

import org.openbaton.nfvo.core.interfaces.HistoryManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lto on 17/10/16.
 */
@Service
public class HistoryInterceptor extends HandlerInterceptorAdapter {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private HistoryManagement historyManagement;

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    //    super.postHandle(request, response, handler, modelAndView);
    log.trace("Calling addHistoryAction");
    historyManagement.addAction(
        request.getMethod(), request.getRequestURI(), String.valueOf(response.getStatus()));
  }
}
