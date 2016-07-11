package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/**
 * Created by lto on 26/01/16.
 */
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
