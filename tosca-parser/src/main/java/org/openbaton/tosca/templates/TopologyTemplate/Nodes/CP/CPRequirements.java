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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by rvl on 17.08.16.
 */
public class CPRequirements {

  private ArrayList<String> virtualLink = new ArrayList<>();
  private ArrayList<String> virtualBinding = new ArrayList<>();

  public CPRequirements(Object reqs) {

    ArrayList<LinkedHashMap<String, String>> resMap =
        (ArrayList<LinkedHashMap<String, String>>) reqs;

    for (LinkedHashMap<String, String> pair : resMap) {

      if (pair.keySet().toArray()[0].equals("virtualLink")) {
        virtualLink.add(pair.get("virtualLink").toString());
      }

      if (pair.keySet().toArray()[0].equals("virtualBinding")) {
        virtualBinding.add(pair.get("virtualBinding").toString());
      }
    }
  }

  public ArrayList<String> getVirtualLink() {
    return virtualLink;
  }

  public void setVirtualLink(ArrayList<String> virtualLink) {
    this.virtualLink = virtualLink;
  }

  public ArrayList<String> getVirtualBinding() {
    return virtualBinding;
  }

  public void setVirtualBinding(ArrayList<String> virtualBinding) {
    this.virtualBinding = virtualBinding;
  }

  @Override
  public String toString() {
    return "CP Requirements: \n"
        + "VirtualBinding: "
        + virtualBinding
        + "\n"
        + "VirtualLink: "
        + virtualLink;
  }
}
