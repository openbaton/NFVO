package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by rvl on 17.08.16.
 */
public class VDUNodeTemplate {

  private String type;
  private String name;
  private HashSet<String> artifacts = new HashSet<>();
  private Object interfaces = null;
  private VDUProperties properties = null;

  public VDUNodeTemplate(NodeTemplate nodeTemplate, String name) {

    this.name = name;
    this.type = nodeTemplate.getType();

    if (nodeTemplate.getArtifacts() != null) {

      Map<String, Object> artifactMap = (Map<String, Object>) nodeTemplate.getArtifacts();

      for (String key : artifactMap.keySet()) {

        VDUArtifact vduArtifact = new VDUArtifact(artifactMap.get(key));

        if (vduArtifact.getType().equals("tosca.artifacts.Deployment.Image.VM")) {
          artifacts.add(vduArtifact.getFile());
        }
      }
    }

    if (nodeTemplate.getProperties() != null) {
      properties = new VDUProperties(nodeTemplate.getProperties());
    }
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public HashSet<String> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(HashSet<String> vduArtifact) {
    this.artifacts = artifacts;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public VDUProperties getProperties() {
    return properties;
  }

  public void setProperties(VDUProperties properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return "Node: \n"
        + "type: "
        + type
        + "\n"
        + "name: "
        + name
        + "\n"
        + "Interfaces: "
        + interfaces
        + "\n"
        + "Artifacts: "
        + artifacts
        + "\n"
        + "Properties: "
        + properties
        + "\n";
  }
}
