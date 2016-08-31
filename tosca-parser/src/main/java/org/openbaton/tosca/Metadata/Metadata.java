package org.openbaton.tosca.Metadata;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;

import java.util.ArrayList;

/**
 * Created by rvl on 26.08.16.
 */
public class Metadata {
  private String name;
  private String scripts_link;
  private Image image;
  private ImageConfig image_config;

  public Metadata(VirtualNetworkFunctionDescriptor vnfd, ArrayList<String> image_names) {

    Image image = new Image(vnfd, image_names);
    ImageConfig image_config = new ImageConfig();

    this.setName(vnfd.getName());
    this.setScripts_link(vnfd.getVnfPackageLocation());

    this.setImage(image);
    //this.setImage_config(image_config);
  }

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
    return image;
  }

  public String getScripts_link() {
    return scripts_link;
  }

  public void setScripts_link(String scripts_link) {
    this.scripts_link = scripts_link;
  }

  public ImageConfig getImage_config() {
    return image_config;
  }

  public void setImage_config(ImageConfig image_config) {
    this.image_config = image_config;
  }
}
