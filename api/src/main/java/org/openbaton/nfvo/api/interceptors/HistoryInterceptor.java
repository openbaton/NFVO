/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.api.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openbaton.nfvo.core.interfaces.HistoryManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Service
public class HistoryInterceptor extends HandlerInterceptorAdapter {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private HistoryManagement historyManagement;

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView) {
    //    super.postHandle(request, response, handler, modelAndView);
    log.trace("Calling addHistoryAction");
    if (!request.getMethod().equalsIgnoreCase("post")
        || !request.getRequestURI().equalsIgnoreCase("/error"))
      historyManagement.addAction(
          request.getMethod(), request.getRequestURI(), String.valueOf(response.getStatus()));
  }
}
