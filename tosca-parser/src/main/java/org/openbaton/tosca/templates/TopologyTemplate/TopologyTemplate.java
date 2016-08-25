package org.openbaton.tosca.templates.TopologyTemplate;

import org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP.CPNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU.VDUNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VL.VLNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF.VNFNodeTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by rvl on 17.08.16.
 */
public class TopologyTemplate {

  private Object inputs = null;
  private Object substitution_mappings = null;
  private Map<String, NodeTemplate> node_templates;

  public Object getInputs() {
    return inputs;
  }

  public void setInputs(Object inputs) {
    this.inputs = inputs;
  }

  public Object getSubstitution_mappings() {
    return substitution_mappings;
  }

  public void setSubstitution_mappings(Object substitution_mappings) {
    this.substitution_mappings = substitution_mappings;
  }

  public Map<String, NodeTemplate> getNode_templates() {
    return node_templates;
  }

  public void setNode_templates(Map<String, NodeTemplate> node_templates) {
    this.node_templates = node_templates;
  }

  public List<CPNodeTemplate> getCPNodes() {

    List<CPNodeTemplate> cpNodes = new ArrayList<CPNodeTemplate>();

    for (String nodeName : node_templates.keySet()) {

      NodeTemplate n = node_templates.get(nodeName);
      if (Objects.equals(n.getType(), "tosca.nodes.nfv.CP")) {

        CPNodeTemplate cpNode = new CPNodeTemplate(n, nodeName);
        cpNodes.add(cpNode);
      }
    }
    return cpNodes;
  }

  public List<VDUNodeTemplate> getVDUNodes() {

    List<VDUNodeTemplate> vduNodes = new ArrayList<>();

    for (String nodeName : node_templates.keySet()) {

      NodeTemplate n = node_templates.get(nodeName);
      if (Objects.equals(n.getType(), "tosca.nodes.nfv.VDU")) {

        VDUNodeTemplate vduNode = new VDUNodeTemplate(n, nodeName);
        vduNodes.add(vduNode);
      }
    }

    return vduNodes;
  }

  public List<VLNodeTemplate> getVLNodes() {

    List<VLNodeTemplate> vlNodes = new ArrayList<>();

    for (String nodeName : node_templates.keySet()) {

      NodeTemplate n = node_templates.get(nodeName);
      if (Objects.equals(n.getType(), "tosca.nodes.nfv.VL")) {

        VLNodeTemplate vduNode = new VLNodeTemplate(n, nodeName);
        vlNodes.add(vduNode);
      }
    }

    return vlNodes;
  }

  public List<VNFNodeTemplate> getVNFNodes() {

    List<VNFNodeTemplate> vnfNodes = new ArrayList<>();

    for (String nodeName : node_templates.keySet()) {

      NodeTemplate n = node_templates.get(nodeName);
      if (Objects.equals(n.getType(), "openbaton.type.VNF")) {

        VNFNodeTemplate vnfNode = new VNFNodeTemplate(n, nodeName);
        vnfNodes.add(vnfNode);
      }
    }

    return vnfNodes;
  }

  @Override
  public String toString() {
    return "Topology: \n"
        + "inuts: "
        + inputs
        + "\n"
        + "substitution_mappings: "
        + substitution_mappings
        + "\n"
        + "Nodes: \n"
        + node_templates;
  }
}
