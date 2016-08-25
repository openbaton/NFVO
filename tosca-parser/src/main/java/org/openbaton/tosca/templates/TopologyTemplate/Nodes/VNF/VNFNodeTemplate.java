package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF;

import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;

import java.util.Map;

/**
 * Created by rvl on 19.08.16.
 */
public class VNFNodeTemplate {

  private String type = "";
  private String name = "";
  private VNFProperties properties = null;
  private VNFInterfaces interfaces = null;
  private VNFRequirements requirements = null;

  public VNFNodeTemplate(NodeTemplate nodeTemplate, String nodeName) {

    this.name = nodeName;
    this.type = nodeTemplate.getType();

    properties = new VNFProperties(nodeTemplate.getProperties());

    requirements = new VNFRequirements(nodeTemplate.getRequirements());

    interfaces = new VNFInterfaces();
    Map<String, Object> interfaceMap = (Map<String, Object>) nodeTemplate.getInterfaces();
    if (interfaceMap.containsKey("lifecycle")) {
      interfaces.setLifecycle(interfaceMap.get("lifecycle"));
    }
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public VNFProperties getProperties() {
    return properties;
  }

  public void setProperties(VNFProperties properties) {
    this.properties = properties;
  }

  public VNFInterfaces getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(VNFInterfaces interfaces) {
    this.interfaces = interfaces;
  }

  public VNFRequirements getRequirements() {
    return requirements;
  }

  public void setRequirements(VNFRequirements requirements) {
    this.requirements = requirements;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {

    return "VNFD Node; "
        + "\n"
        + "type "
        + type
        + "\n"
        + "prop: "
        + properties
        + "\n"
        + "reqs: "
        + requirements
        + "\n"
        + "interfaces: "
        + interfaces
        + "\n";
  }
}
