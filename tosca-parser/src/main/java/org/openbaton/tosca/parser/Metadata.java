package org.openbaton.tosca.parser;

/**
 * Created by rvl on 26.08.16.
 */
public class Metadata {
  public String name;
  public Image image;

  public Metadata() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public Image getImage() {
    if (image == null) return new Image();
    else return image;
  }

  @Override
  public String toString() {
    return "Metadata{" + "name='" + name + '\'' + ", image=" + image + '}';
  }
}
