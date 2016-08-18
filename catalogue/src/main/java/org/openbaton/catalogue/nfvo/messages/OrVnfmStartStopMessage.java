package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

/**
 * Created by fmu on 17/08/16.
 */
public class OrVnfmStartStopMessage extends OrVnfmMessage {

  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private VNFCInstance vnfcInstance;
  private VNFRecordDependency vnfrDependency;

  @Override
  public String toString() {
    return "OrVnfmScalingMessage{"
        + "virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + ", vnfcInstance="
        + vnfcInstance
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
