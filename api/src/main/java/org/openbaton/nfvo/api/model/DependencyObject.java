package org.openbaton.nfvo.api.model;

/**
 * Created by mob on 12.04.16.
 */
public class DependencyObject {
  private String source;
  private String target;

  public DependencyObject() {}

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

  @Override
  public String toString() {
    return "DependencyObject{" + "source='" + source + '\'' + ", target='" + target + '\'' + '}';
  }
}
