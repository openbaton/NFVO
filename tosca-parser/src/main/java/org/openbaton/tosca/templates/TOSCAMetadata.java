package org.openbaton.tosca.templates;

/**
 * Created by rvl on 17.08.16.
 */
public class TOSCAMetadata {

  private String ID;
  private String vendor;
  private String version;

  public String getID() {
    return ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
