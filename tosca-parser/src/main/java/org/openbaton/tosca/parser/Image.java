package org.openbaton.tosca.parser;

/**
 * Created by rvl on 26.08.16.
 */
import java.util.ArrayList;
import java.util.List;

public class Image {
  private String upload;
  private List<String> names;
  private List<String> ids;
  private String link;

  public Image() {}

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
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

  public List<String> getNames() {

    if (this.names == null) this.names = new ArrayList<>();

    return names;
  }

  public void setNames(List<String> names) {
    this.names = names;
  }

  @Override
  public String toString() {
    return "Image{"
        + "upload='"
        + upload
        + '\''
        + ", names="
        + names
        + ", ids="
        + ids
        + ", link='"
        + link
        + '\''
        + '}';
  }
}
