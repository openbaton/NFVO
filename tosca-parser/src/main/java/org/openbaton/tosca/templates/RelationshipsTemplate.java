package org.openbaton.tosca.templates;

import java.util.ArrayList;

/**
 * Created by rvl on 22.08.16.
 */
public class RelationshipsTemplate {

  private String type;
  private String source;
  private String target;
  private ArrayList<String> parameters;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public ArrayList<String> getParameters() {
    return parameters;
  }

  public void setParameters(ArrayList<String> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {

    return "Relationships: "
        + "\n"
        + "source: "
        + source
        + "\n"
        + "target: "
        + target
        + "\n"
        + "Parameters "
        + parameters
        + "\n";
  }
}
