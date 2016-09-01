package org.openbaton.tosca.Metadata;

/**
 * Created by rvl on 26.08.16.
 */
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Image {
  private String upload;
  private ArrayList<String> names = new ArrayList<>();
  private String link;

  public Image(VirtualNetworkFunctionDescriptor vnfd, ArrayList<String> image_names) {
    this.setUpload("false");

    Set<VirtualDeploymentUnit> vdus = vnfd.getVdu();

    for (VirtualDeploymentUnit vdu : vdus) {
      for (String imageString : vdu.getVm_image())
        if (!this.getNames().contains(imageString)) this.getNames().add(imageString);
    }

    for (String name : image_names) {
      names.add(name);
    }
    //link = "http://releases.ubuntu.com/14.04/ubuntu-14.04.4-desktop-amd64.iso";
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getUpload() {
    return upload;
  }

  public void setUpload(String upload) {
    this.upload = upload;
  }

  public ArrayList<String> getNames() {

    return names;
  }

  public void setNames(ArrayList<String> names) {
    this.names = names;
  }

  public Map<String, Object> toHashMap() {

    Map<String, Object> data = new HashMap<>();

    data.put("names", names);
    data.put("upload", upload);
    data.put("link", link);

    return data;
  }

  @Override
  public String toString() {
    return "Image{"
        + "upload='"
        + upload
        + '\''
        + ", names="
        + names
        + ", link='"
        + link
        + '\''
        + '}';
  }
}
