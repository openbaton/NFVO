/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
