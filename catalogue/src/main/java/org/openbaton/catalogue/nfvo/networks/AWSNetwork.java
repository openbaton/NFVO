/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.catalogue.nfvo.networks;
//The network is still mapped to amazon subnet due to the topology structure

import javax.persistence.Entity;

@Entity
public class AWSNetwork extends BaseNetwork {
  private String ipv4cidr;
  private String state;
  private String vpcId;
  private String avZone;
  private boolean def;

  public String getIpv4cidr() {
    return ipv4cidr;
  }

  public void setIpv4cidr(String ipv4cidr) {
    this.ipv4cidr = ipv4cidr;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getVpcId() {
    return vpcId;
  }

  public void setVpcId(String vpcId) {
    this.vpcId = vpcId;
  }

  public String getAvZone() {
    return avZone;
  }

  public void setAvZone(String avZone) {
    this.avZone = avZone;
  }

  public boolean isDef() {
    return def;
  }

  public void setDef(boolean def) {
    this.def = def;
  }

  @Override
  public String toString() {
    return "AWSNetwork{"
        + "state='"
        + state
        + '\''
        + ", vpcId='"
        + vpcId
        + '\''
        + ", avZone='"
        + avZone
        + '\''
        + ", ipv4cidr='"
        + '\''
        + ipv4cidr
        + ", default='"
        + '\''
        + def
        + '\''
        + "} "
        + super.toString();
  }
}
