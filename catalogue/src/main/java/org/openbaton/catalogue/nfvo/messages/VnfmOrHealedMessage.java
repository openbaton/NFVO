package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/**
 * Created by mob on 08.02.16.
 */
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
