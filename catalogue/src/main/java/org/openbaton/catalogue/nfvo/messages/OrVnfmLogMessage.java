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

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/** Used to send a request to a VNFM asking for log files. */
public class OrVnfmLogMessage extends OrVnfmMessage {

  private String vnfrName;
  private String hostname;

  public OrVnfmLogMessage(String vnfrName, String hostname) {
    this.action = Action.LOG_REQUEST;
    this.vnfrName = vnfrName;
    this.hostname = hostname;
  }

  public String getVnfrName() {
    return vnfrName;
  }

  public void setVnfrName(String vnfrName) {
    this.vnfrName = vnfrName;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
}
