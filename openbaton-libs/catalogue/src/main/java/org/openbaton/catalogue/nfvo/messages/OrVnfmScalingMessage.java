package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

import java.util.Map;

/**
 * Created by lto on 13/10/15.
 */
public class OrVnfmScalingMessage extends OrVnfmMessage {

  private VNFComponent component;
  private VNFCInstance vnfcInstance;
  private VimInstance vimInstance;
  private VNFPackage vnfPackage;
  private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
  private VNFRecordDependency dependency;
  private String mode;
  private Map<String, String> extension;

  @Override
  public String toString() {
    return "OrVnfmScalingMessage{"
        + "component="
        + component
        + ", vnfcInstance="
        + vnfcInstance
        + ", vimInstance="
        + vimInstance
        + ", vnfPackage="
        + vnfPackage
        + ", virtualNetworkFunctionRecord="
        + virtualNetworkFunctionRecord
        + ", dependency="
        + dependency
        + ", mode='"
        + mode
        + '\''
        + ", extension="
        + extension
        + "} "
        + super.toString();
  }

  public Map<String, String> getExtension() {
    return extension;
  }

  public void setExtension(Map<String, String> extension) {
    this.extension = extension;
  }

  public VNFPackage getVnfPackage() {
    return vnfPackage;
  }

  public void setVnfPackage(VNFPackage vnfPackage) {
    this.vnfPackage = vnfPackage;
  }

  public VimInstance getVimInstance() {
    return vimInstance;
  }

  public void setVimInstance(VimInstance vimInstance) {
    this.vimInstance = vimInstance;
  }

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }

  public VNFComponent getComponent() {
    return component;
  }

  public void setComponent(VNFComponent component) {
    this.component = component;
  }

  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
    return virtualNetworkFunctionRecord;
  }

  public void setVirtualNetworkFunctionRecord(
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
    this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public VNFRecordDependency getDependency() {
    return dependency;
  }

  public void setDependency(VNFRecordDependency dependency) {
    this.dependency = dependency;
  }
}
