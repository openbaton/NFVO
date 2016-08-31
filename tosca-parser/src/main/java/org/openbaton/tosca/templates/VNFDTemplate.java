package org.openbaton.tosca.templates;

import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF.VNFProperties;
import org.openbaton.tosca.templates.TopologyTemplate.TopologyTemplate;

/**
 * Created by rvl on 17.08.16.
 */
public class VNFDTemplate {

  private String tosca_definitions_version;
  private String description = "";
  private TOSCAMetadata metadata;
  private Object node_types = null;
  private TopologyTemplate topology_template;
  private VNFProperties inputs;

  public String getTosca_definitions_version() {
    return tosca_definitions_version;
  }

  public void setTosca_definitions_version(String tosca_definitions_version) {
    this.tosca_definitions_version = tosca_definitions_version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TOSCAMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(TOSCAMetadata metadata) {
    this.metadata = metadata;
  }

  public Object getNode_types() {
    return node_types;
  }

  public void setNode_types(Object node_types) {
    this.node_types = node_types;
  }

  public void setTopology_template(TopologyTemplate topology_template) {
    this.topology_template = topology_template;
  }

  public TopologyTemplate getTopology_template() {
    return this.topology_template;
  }

  public void setInputs(VNFProperties inputs) {
    this.inputs = inputs;
  }

  public VNFProperties getInputs() {
    return inputs;
  }

  @Override
  public String toString() {

    return "tosca_definitions_version"
        + tosca_definitions_version
        + "\n"
        + "description"
        + description
        + "\n"
        + "Metadata: "
        + metadata
        + "\n"
        + "node_types: "
        + node_types
        + "\n"
        + "topology_template: "
        + topology_template
        + "\n"
        + "inputs_template: "
        + inputs;
  }
}
