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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Created by rvl on 19.08.16. */
public class VDUProperties {

  private int scale_in_out;
  private Set<String> vim_instance_name;
  private VDUFaultManagement fault_management_policy = null;
  private VDUHighAvailability high_availability = null;
  private Set<String> monitoring_parameter;

  @SuppressWarnings({"unsafe", "unchecked"})
  public VDUProperties(Object vduProp) {

    Map<String, Object> vduPropMap = (Map<String, Object>) vduProp;

    if (vduPropMap.containsKey("scale_in_out")) {
      scale_in_out = (Integer) vduPropMap.get("scale_in_out");
    }

    if (vduPropMap.containsKey("vim_instance_name")) {
      vim_instance_name =
          new LinkedHashSet<String>() {
            {
              addAll((Collection<? extends String>) vduPropMap.get("vim_instance_name"));
            }
          };
    }

    if (vduPropMap.containsKey("fault_management_policy")) {
      fault_management_policy = new VDUFaultManagement(vduPropMap.get("fault_management_policy"));
    }
    
    if (vduPropMap.containsKey("high_availability")) {
      high_availability = new VDUHighAvailability(vduPropMap.get("high_availability"));
    }

    if (vduPropMap.containsKey("monitoring_parameter")) {
      monitoring_parameter =
          new LinkedHashSet<String>() {
            {
              addAll((Collection<? extends String>) vduPropMap.get("monitoring_parameter"));
            }
          };
    }

  }

  public int getScale_in_out() {
    return scale_in_out;
  }

  public void setScale_in_out(int scale_in_out) {
    this.scale_in_out = scale_in_out;
  }

  public Set<String> getVim_instance_name() {
    return vim_instance_name;
  }

  public void setVim_instance_name(LinkedHashSet<String> vim_instance_name) {
    this.vim_instance_name = vim_instance_name;
  }

  @Override
  public String toString() {
    return "VDU Properties: "
        + "\n"
        + "scale_in_out: "
        + scale_in_out
        + "\n"
        + "vim instance name: "
        + vim_instance_name
        + "\n";
  }

  public VDUFaultManagement getFault_management_policy() {
    return fault_management_policy;
  }

  public void setFault_management_policy(VDUFaultManagement fault_management_policy) {
    this.fault_management_policy = fault_management_policy;
  }
  public VDUHighAvailability getHigh_Availability() {
    return high_availability;
  }

  public void setHighAvailability(VDUHighAvailability high_availability) {
    this.high_availability = high_availability;
  }

  public Set<String> getMonitoring_Parameter() {
    return monitoring_parameter;
  }

  public void setMonitoring_Parameter(LinkedHashSet<String> monitoring_parameter) {
    this.monitoring_parameter = monitoring_parameter;
  }

}
