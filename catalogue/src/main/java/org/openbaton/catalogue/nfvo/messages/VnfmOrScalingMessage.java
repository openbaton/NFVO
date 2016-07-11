package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/**
 * Created by lto on 13/04/16.
 */
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
