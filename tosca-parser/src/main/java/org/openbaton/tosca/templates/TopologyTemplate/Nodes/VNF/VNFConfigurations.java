/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
