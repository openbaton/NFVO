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
import java.util.LinkedHashMap;

/** Created by rvl on 19.08.16. */
public class VNFRequirements {

  private ArrayList<String> virtualLinks = new ArrayList<>();
  private ArrayList<String> vdus = new ArrayList<>();

  public VNFRequirements(Object reqs) {

    ArrayList<LinkedHashMap<String, String>> resMap =
        (ArrayList<LinkedHashMap<String, String>>) reqs;

    for (LinkedHashMap<String, String> pair : resMap) {

      if (pair.keySet().toArray()[0].equals("virtualLink")) {
        virtualLinks.add(pair.get("virtualLink"));
      }

      if (pair.keySet().toArray()[0].equals("vdu")) {
        vdus.add(pair.get("vdu"));
      }
    }
  }

  public ArrayList<String> getVirtualLinks() {
    return virtualLinks;
  }

  public void setVirtualLinks(ArrayList<String> virtualLinks) {
    this.virtualLinks = virtualLinks;
  }

  public ArrayList<String> getVDUS() {
    return vdus;
  }

  public void setVDUS(ArrayList<String> forwarders) {
    this.vdus = forwarders;
  }

  @Override
  public String toString() {
    return "links: " + virtualLinks + "\n" + "forwarders" + vdus + "\n";
  }
}
