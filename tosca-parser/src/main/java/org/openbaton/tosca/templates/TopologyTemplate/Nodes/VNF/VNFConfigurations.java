package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rvl on 22.08.16.
 */
public class VNFConfigurations {

  private String name = "";
  private ArrayList<HashMap<String, String>> configurationParameters = null;

  public VNFConfigurations() {}

  public VNFConfigurations(Object config) {

    Map<String, Object> configMap = (Map<String, Object>) config;

    if (configMap.containsKey("name")) {
      this.name = (String) configMap.get("name");
    }

    if (configMap.containsKey("configurationParameters")) {
      this.configurationParameters =
          (ArrayList<HashMap<String, String>>) configMap.get("configurationParameters");
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<HashMap<String, String>> getConfigurationParameters() {
    return configurationParameters;
  }

  public void setConfigurationParameters(
      ArrayList<HashMap<String, String>> configurationParameters) {
    this.configurationParameters = configurationParameters;
  }

  @Override
  public String toString() {
    return "Configuration{"
        + "\n"
        + "name='"
        + name
        + "\n"
        + ", configurationParameters="
        + configurationParameters
        + '}';
  }
}
