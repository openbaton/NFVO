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

package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/** Created by mob on 14.09.15. */
public class OrVnfmErrorMessage extends OrVnfmMessage {

  private VirtualNetworkFunctionRecord vnfr;
  private String message;

  public OrVnfmErrorMessage() {
    this.action = Action.ERROR;
  }

  public OrVnfmErrorMessage(VirtualNetworkFunctionRecord vnfr, String message) {
    this.vnfr = vnfr;
    this.message = message;
    this.action = Action.ERROR;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public VirtualNetworkFunctionRecord getVnfr() {
    return vnfr;
  }

  public void setVnfr(VirtualNetworkFunctionRecord vnfr) {
    this.vnfr = vnfr;
  }

  @Override
  public String toString() {
    return "OrVnfmErrorMessage{"
        + "action='"
        + Action.ERROR
        + '\''
        + "message='"
        + message
        + '\''
        + ", vnfr="
        + vnfr
        + '}';
  }
}
