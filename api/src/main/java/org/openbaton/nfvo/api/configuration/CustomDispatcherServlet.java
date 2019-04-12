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
