package org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP;

import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;

/**
 * Created by rvl on 17.08.16.
 */
public class CPNodeTemplate {

  private String name = null;
  private String type = null;
  private CPProperties properties = null;
  private CPRequirements requirements = null;

  public CPNodeTemplate(NodeTemplate nodeTemplate, String name) {
    this.type = nodeTemplate.getType();

    if (nodeTemplate.getProperties() != null) {
      this.properties = new CPProperties(nodeTemplate.getProperties());
    }

    if (nodeTemplate.getRequirements() != null) {
      this.requirements = new CPRequirements(nodeTemplate.getRequirements());
    }
  }

  public CPProperties getProperties() {
    return properties;
  }

  public void setProperties(CPProperties properties) {
    this.properties = properties;
  }

  public CPRequirements getRequirements() {
    return requirements;
  }

  public void setRequirements(CPRequirements requirements) {
    this.requirements = requirements;
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

  @Override
  public String toString() {
    return "CP Node: \n"
        + "name: "
        + name
        + "\n"
        + "type: "
        + type
        + "\n"
        + "Properties: "
        + properties
        + "\n"
        + "Requirements: "
        + requirements;
  }
}
