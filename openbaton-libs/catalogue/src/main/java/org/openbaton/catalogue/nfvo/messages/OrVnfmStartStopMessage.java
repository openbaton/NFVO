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
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/** Created by fmu on 17/08/16. */
public class OrVnfmStartStopMessage extends OrVnfmMessage {

  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private VNFCInstance vnfcInstance;
  private VNFRecordDependency vnfrDependency;

  public OrVnfmStartStopMessage() {}

  public OrVnfmStartStopMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.vnfcInstance = vnfcInstance;
  }

  public OrVnfmStartStopMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFCInstance vnfcInstance,
      Action action) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.vnfcInstance = vnfcInstance;
    this.action = action;
  }

  public OrVnfmStartStopMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFCInstance vnfcInstance,
      VNFRecordDependency vnfrDependency) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.vnfcInstance = vnfcInstance;
    this.vnfrDependency = vnfrDependency;
  }

  public OrVnfmStartStopMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFCInstance vnfcInstance,
      VNFRecordDependency vnfrDependency,
      Action action) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.vnfcInstance = vnfcInstance;
    this.vnfrDependency = vnfrDependency;
    this.action = action;
  }

  @Override
  public String toString() {
    return "OrVnfmStartStopMessage{"
        + "virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + ", vnfcInstance="
        + vnfcInstance
        + ", vnfRecordDependency="
        + vnfrDependency
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

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }

  public VNFRecordDependency getVnfrd() {
    return vnfrDependency;
  }

  public void setVnfrd(VNFRecordDependency vnfrDependency) {
    this.vnfrDependency = vnfrDependency;
  }
}
