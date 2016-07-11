/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by moritz on 02.12.15.
 */
public class OrVnfmHealVNFRequestMessage extends OrVnfmMessage {
  private VNFCInstance vnfcInstance;
  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private String cause;

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }

  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
    return virtualNetworkFunctionRecord;
  }

  public void setVirtualNetworkFunctionRecord(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
  }

  public String getCause() {
    return cause;
  }

  public void setCause(String cause) {
    this.cause = cause;
  }

  @Override
  public String toString() {
    return "OrVnfmHealVNFRequestMessage{"
        + "vnfcInstance="
        + vnfcInstance
        + ", virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + ", cause='"
        + cause
        + '\''
        + '}';
  }
}
