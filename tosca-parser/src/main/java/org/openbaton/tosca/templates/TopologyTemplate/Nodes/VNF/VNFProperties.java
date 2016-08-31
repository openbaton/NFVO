package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF;

import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;

import java.util.*;

/**
 * Created by rvl on 19.08.16.
 */
public class VNFProperties {

  private String vendor = null;
  private String ID = null;
  private double version = 0.0;
  private String vnfPackageLocation;
  private ArrayList<HashMap<String, String>> deploymentFlavour;
  private VNFConfigurations configurations = null;
  private String endpoint = null;
  private String type = null;
  private VNFInterfaces interfaces = null;

  public VNFProperties() {}

  public VNFProperties(Object properties) {

    Map<String, Object> propertiesMap = (Map<String, Object>) properties;

    if (propertiesMap.containsKey("vendor")) {
      vendor = (String) propertiesMap.get("vendor");
    }

    if (propertiesMap.containsKey("version")) {
      version = (double) propertiesMap.get("version");
    }

    if (propertiesMap.containsKey("vnfPackageLocation")) {
      vnfPackageLocation = (String) propertiesMap.get("vnfPackageLocation");
    }

    if (propertiesMap.containsKey("deploymentFlavour")) {
      deploymentFlavour =
          (ArrayList<HashMap<String, String>>) propertiesMap.get("deploymentFlavour");
    }

    if (propertiesMap.containsKey("ID")) {
      ID = (String) propertiesMap.get("ID");
    }

    if (propertiesMap.containsKey("configurations")) {
      configurations = new VNFConfigurations(propertiesMap.get("configurations"));
    }

    if (propertiesMap.containsKey("endpoint")) {
      endpoint = (String) propertiesMap.get("endpoint");
    }

    if (propertiesMap.containsKey("type")) {
      type = (String) propertiesMap.get("type");
    }
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getID() {
    return ID;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  public double getVersion() {
    return version;
  }

  public void setVersion(double version) {
    this.version = version;
  }

  public String getVnfPackageLocation() {
    return vnfPackageLocation;
  }

  public void setVnfPackageLocation(String vnfPackageLocation) {
    this.vnfPackageLocation = vnfPackageLocation;
  }

  public ArrayList<HashMap<String, String>> getDeploymentFlavour() {
    return deploymentFlavour;
  }

  public void setDeploymentFlavour(ArrayList<HashMap<String, String>> deploymentFlavour) {
    this.deploymentFlavour = deploymentFlavour;
  }

  public Set<VNFDeploymentFlavour> getDeploymentFlavourConverted() {

    Set<VNFDeploymentFlavour> vnfdf = new HashSet<>();

    if (deploymentFlavour != null) {
      for (HashMap<String, String> df : this.deploymentFlavour) {

        for (String key : df.keySet()) {
          if (key.equals("flavour_key")) {

            VNFDeploymentFlavour new_df = new VNFDeploymentFlavour();
            new_df.setFlavour_key(df.get("flavour_key"));
            vnfdf.add(new_df);
          }
        }
      }
    }

    return vnfdf;
  }

  public VNFConfigurations getConfigurations() {
    return configurations;
  }

  public void setConfigurations(VNFConfigurations configurations) {
    this.configurations = configurations;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public VNFInterfaces getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(VNFInterfaces interfaces) {
    this.interfaces = interfaces;
  }

  @Override
  public String toString() {

    return "VNFP : "
        + "\n"
        + "id : "
        + getID()
        + "\n"
        + "vendor : "
        + vendor
        + "\n"
        + "version : "
        + version
        + "\n"
        + "package : "
        + vnfPackageLocation
        + "\n"
        + "depl flavour : "
        + deploymentFlavour
        + "\n";
  }
}
