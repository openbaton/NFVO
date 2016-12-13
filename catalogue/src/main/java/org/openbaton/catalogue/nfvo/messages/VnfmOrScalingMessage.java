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
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/** Created by lto on 13/04/16. */
public class VnfmOrScalingMessage extends VnfmOrMessage {
  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private String userData;

  public VnfmOrScalingMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, String userData) {
    this.action = Action.SCALING;
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.userData = userData;
  }

  public VnfmOrScalingMessage() {
    this.setAction(Action.SCALING);
  }

  @Override
  public String toString() {
    return "VnfmOrScalingMessage{"
        + "virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + ", userData='"
        + userData
        + '\''
        + "} "
        + super.toString();
  }

  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
    return virtualNetworkFunctionRecord;
  }

  public void setVirtualNetworkFunctionRecord(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
  }

  public String getUserData() {
    return userData;
  }

  public void setUserData(String userData) {
    this.userData = userData;
  }
}
