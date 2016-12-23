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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VL;

import java.util.Map;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;

/** Created by rvl on 19.08.16. */
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
