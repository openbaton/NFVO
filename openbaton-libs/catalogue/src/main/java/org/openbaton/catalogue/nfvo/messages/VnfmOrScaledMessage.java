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

/** Created by mob on 14.09.15. */
public class VnfmOrScaledMessage extends VnfmOrMessage {

  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private VNFCInstance vnfcInstance;

  public VnfmOrScaledMessage(VirtualNetworkFunctionRecord vnfr) {
    this.virtualNetworkFunctionRecord = vnfr;
    this.action = Action.SCALED;
  }

  public VnfmOrScaledMessage() {}

  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
    return virtualNetworkFunctionRecord;
  }

  public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord vnfr) {
    this.virtualNetworkFunctionRecord = vnfr;
  }

  @Override
  public String toString() {
    return "VnfmOrScaledMessage{"
        + "virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + ", vnfcInstance="
        + vnfcInstance
        + '}';
  }

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }
}
