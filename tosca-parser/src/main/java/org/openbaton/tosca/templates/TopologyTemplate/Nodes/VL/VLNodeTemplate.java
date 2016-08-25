package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VL;

import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;

import java.util.Map;

/**
 * Created by rvl on 19.08.16.
 */
public class VLNodeTemplate {

  private String type = "";
  private String name = "";
  private String vendor = "";

  public VLNodeTemplate(NodeTemplate vl, String name) {

    this.name = name;

    this.type = vl.getType();

    if (vl.getProperties() != null) {

      Map<String, Object> propertiesMap = (Map<String, Object>) vl.getProperties();

      if (propertiesMap.containsKey("vendor")) {

        this.vendor = (String) propertiesMap.get("vendor");
      }
    }
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String toString() {

    return "VL: "
        + "\n"
        + "type: "
        + type
        + "\n"
        + "name: "
        + name
        + "\n"
        + "vendor: "
        + vendor
        + "\n";
  }
}
