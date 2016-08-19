package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.VnfmOrMessage;

/**
 * Created by fmu on 18/08/16.
 */
public class VnfmOrStartStopMessage extends VnfmOrMessage {

  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private VNFCInstance vnfcInstance;
  private VNFRecordDependency vnfrDependency;

  public VnfmOrStartStopMessage() {}

  public VnfmOrStartStopMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VNFCInstance vnfcInstance) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.vnfcInstance = vnfcInstance;
  }

  public VnfmOrStartStopMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFCInstance vnfcInstance,
      Action action) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.vnfcInstance = vnfcInstance;
    this.action = action;
  }

  public VnfmOrStartStopMessage(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VNFCInstance vnfcInstance,
      VNFRecordDependency vnfrDependency) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    this.vnfcInstance = vnfcInstance;
    this.vnfrDependency = vnfrDependency;
  }

  public VnfmOrStartStopMessage(
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
    return "VnfmOrStartStopMessage{"
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
