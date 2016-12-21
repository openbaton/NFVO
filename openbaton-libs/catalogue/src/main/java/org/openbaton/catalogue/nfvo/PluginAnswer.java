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

package org.openbaton.catalogue.nfvo;

import java.io.Serializable;

/** Created by lto on 13/08/15. */
public class PluginAnswer implements Serializable {
  private Serializable answer;
  private Throwable exception;

  public Throwable getException() {
    return exception;
  }

  public void setException(Throwable exception) {
    this.exception = exception;
  }

  public Serializable getAnswer() {
    return answer;
  }

  public void setAnswer(Serializable answer) {
    this.answer = answer;
  }

  @Override
  public String toString() {
    return "PluginAnswer{"
        + "answer="
        + answer
        + ", exception="
        + (exception == null ? exception : exception.getMessage())
        + '}';
  }
}
