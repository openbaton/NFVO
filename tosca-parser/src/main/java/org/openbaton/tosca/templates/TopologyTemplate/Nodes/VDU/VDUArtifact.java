package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import java.util.Map;

/**
 * Created by rvl on 18.08.16.
 */
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
