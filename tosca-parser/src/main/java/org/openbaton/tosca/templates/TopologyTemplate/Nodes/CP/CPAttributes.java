package org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP;

import java.util.Map;

/**
 * Created by rvl on 17.08.16.
 */
public class CPAttributes {

  private String address = null;

  public CPAttributes(Object attributes) {
    Map<String, String> attributesMap = (Map<String, String>) attributes;

    if (attributesMap.containsKey("address")) {
      this.address = attributesMap.get("address");
    }
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  @Override
  public String toString() {
    return "CP Attributes: \n" + "Address: " + address + "\n";
  }
}
