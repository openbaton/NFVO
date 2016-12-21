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

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/** Created by mob on 08.02.16. */
public class VnfmOrHealedMessage extends VnfmOrMessage {

  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private VNFCInstance vnfcInstance;
  private String cause;

  public VnfmOrHealedMessage(VirtualNetworkFunctionRecord vnfr) {
    this.virtualNetworkFunctionRecord = vnfr;
    this.action = Action.HEAL;
  }

  public VnfmOrHealedMessage() {}

  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
    return virtualNetworkFunctionRecord;
  }

  public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord vnfr) {
    this.virtualNetworkFunctionRecord = vnfr;
  }

  public String getCause() {
    return cause;
  }

  public void setCause(String cause) {
    this.cause = cause;
  }

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }

  @Override
  public String toString() {
    return "VnfmOrHealedMessage{"
        + "virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + ", vnfcInstance="
        + vnfcInstance
        + ", cause='"
        + cause
        + '\''
        + "} "
        + super.toString();
  }
}
