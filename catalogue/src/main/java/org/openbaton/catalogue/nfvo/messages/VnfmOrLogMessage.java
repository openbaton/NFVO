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

package org.openbaton.catalogue.nfvo.messages;

import java.util.List;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/** Used to send back an answer to an NFVO's request asking for log files. */
public class VnfmOrLogMessage extends VnfmOrMessage {

  private List<String> outputLog;
  private List<String> errorLog;

  public VnfmOrLogMessage() {
    this.action = Action.LOG_REQUEST;
  }

  public VnfmOrLogMessage(List<String> outputLog, List<String> errorLog) {
    this.outputLog = outputLog;
    this.errorLog = errorLog;
    this.action = Action.LOG_REQUEST;
  }

  public List<String> getOutputLog() {
    return outputLog;
  }

  public void setOutputLog(List<String> outputLog) {
    this.outputLog = outputLog;
  }

  public List<String> getErrorLog() {
    return errorLog;
  }

  public void setErrorLog(List<String> errorLog) {
    this.errorLog = errorLog;
  }
}
