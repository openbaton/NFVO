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

import java.util.Map;

/** Created by rvl on 18.08.16. */
public class VDUArtifact {

  private String type;
  private String file;

  public VDUArtifact(Object artifact) {

    Map<String, String> artifactMap = (Map<String, String>) artifact;

    if (artifactMap.containsKey("type")) {
      this.type = artifactMap.get("type");
    }

    if (artifactMap.containsKey("file")) {
      this.file = artifactMap.get("file");
    }
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  @Override
  public String toString() {
    return "Artifact: \n" + "type: " + type + "\n" + "file: " + file;
  }
}
