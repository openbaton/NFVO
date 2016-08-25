package org.openbaton.tosca.templates;

import org.openbaton.tosca.templates.TopologyTemplate.TopologyTemplate;

import java.util.Map;

/**
 * Created by rvl on 17.08.16.
 */
public class NSDTemplate {

  private String tosca_definitions_version;
  private String tosca_default_namespace;
  private String description = "";
  private TOSCAMetadata metadata;
  private Object node_types = null;
  private TopologyTemplate topology_template;
  private Map<String, RelationshipsTemplate> relationships_template;

  public String getTosca_definitions_version() {
    return tosca_definitions_version;
  }

  public void setTosca_definitions_version(String tosca_definitions_version) {
    this.tosca_definitions_version = tosca_definitions_version;
  }

  public String getTosca_default_namespace() {
    return tosca_default_namespace;
  }

  public void setTosca_default_namespace(String tosca_default_namespace) {
    this.tosca_default_namespace = tosca_default_namespace;
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

  public TopologyTemplate getTopology_template() {
    return topology_template;
  }

  public void setTopology_template(TopologyTemplate topology_template) {
    this.topology_template = topology_template;
  }

  public Map<String, RelationshipsTemplate> getRelationships_template() {
    return relationships_template;
  }

  public void setRelationships_template(Map<String, RelationshipsTemplate> relationships_template) {
    this.relationships_template = relationships_template;
  }
}
