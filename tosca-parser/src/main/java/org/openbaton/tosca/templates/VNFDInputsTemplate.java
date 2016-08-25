package org.openbaton.tosca.templates;

import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF.VNFInterfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rvl on 23.08.16.
 */
public class VNFDInputsTemplate {

  private String vnfPackageLocation = null;
  private ArrayList<HashMap<String, String>> deploymentFlavour = null;
  private VNFInterfaces interfaces = null;
  private Object configurations = null;
  private String endpoint = null;
  private String type = null;

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

  public VNFInterfaces getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(VNFInterfaces vnfInterfaces) {
    this.interfaces = vnfInterfaces;
  }

  public Object getConfigurations() {
    return configurations;
  }

  public void setConfigurations(Object configurations) {
    this.configurations = configurations;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public String toString() {

    return "vnf_interfaces: " + interfaces + "\n" + "vnfPackageLoc: " + vnfPackageLocation + "\n";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
