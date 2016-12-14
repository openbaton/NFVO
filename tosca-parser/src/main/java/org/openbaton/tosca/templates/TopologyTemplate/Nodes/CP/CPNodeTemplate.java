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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP;

import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;

/** Created by rvl on 17.08.16. */
public class CPNodeTemplate {

  private String name = null;
  private String type = null;
  private CPProperties properties = null;
  private CPRequirements requirements = null;

  public CPNodeTemplate(NodeTemplate nodeTemplate) {
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
