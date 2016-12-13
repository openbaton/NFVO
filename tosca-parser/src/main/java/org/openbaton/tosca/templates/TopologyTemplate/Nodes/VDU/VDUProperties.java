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

import java.util.ArrayList;
import java.util.Map;

/** Created by rvl on 19.08.16. */
public class VDUProperties {

  private ArrayList<String> vm_image;
  private int scale_in_out;
  private ArrayList<String> vim_instance_name;

  public VDUProperties(Object vduProp) {

    Map<String, Object> vduPropMap = (Map<String, Object>) vduProp;

    if (vduPropMap.containsKey("vm_image")) {
      vm_image = (ArrayList<String>) vduPropMap.get("vm_image");
    }

    if (vduPropMap.containsKey("scale_in_out")) {
      scale_in_out = (Integer) vduPropMap.get("scale_in_out");
    }

    if (vduPropMap.containsKey("vim_instance_name")) {
      vim_instance_name = (ArrayList<String>) vduPropMap.get("vim_instance_name");
    }
  }

  public ArrayList<String> getVm_image() {
    return vm_image;
  }

  public void setVm_image(ArrayList<String> vm_image) {
    this.vm_image = vm_image;
  }

  public int getScale_in_out() {
    return scale_in_out;
  }

  public void setScale_in_out(int scale_in_out) {
    this.scale_in_out = scale_in_out;
  }

  public ArrayList<String> getVim_instance_name() {
    return vim_instance_name;
  }

  public void setVim_instance_name(ArrayList<String> vim_instance_name) {
    this.vim_instance_name = vim_instance_name;
  }

  @Override
  public String toString() {
    return "VDU Properties: "
        + "\n"
        + "vm_image: "
        + vm_image
        + "\n"
        + "scale_in_out: "
        + scale_in_out
        + "\n"
        + "vim instance name: "
        + vim_instance_name
        + "\n";
  }
}
