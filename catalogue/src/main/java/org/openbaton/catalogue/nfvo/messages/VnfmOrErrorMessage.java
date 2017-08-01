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
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/** Created by lto on 26/01/16. */
public class VnfmOrErrorMessage extends VnfmOrMessage {
  private String nsrId;
  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private Exception exception;

  public VnfmOrErrorMessage(
      Exception exception,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      String nsrId) {
    this.exception = exception;
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.nsrId = nsrId;
  }

  public VnfmOrErrorMessage() {}

  public String getNsrId() {
    return nsrId;
  }

  public void setNsrId(String nsrId) {
    this.nsrId = nsrId;
  }

  public Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
    return virtualNetworkFunctionRecord;
  }

  public void setVirtualNetworkFunctionRecord(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
  }

  @Override
  public String toString() {
    return "VnfmOrErrorMessage{"
        + "exception="
        + exception
        + ", nsrId='"
        + nsrId
        + '\''
        + ", virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + '}';
  }
}
