/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.vnfm_reg.tasks;

/** Created by lto on 12/04/16. */
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class TaskExceptionHandler implements AsyncUncaughtExceptionHandler {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... params) {
    log.error("Method Name::" + method.getName());
    log.error("Exception occurred::" + ex);
    ex.printStackTrace();
  }
}
