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

package org.openbaton.catalogue.nfvo.networks;

import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
public class Subnet extends BaseEntity {
  private String name;
  private String extId;
  private String networkId;
  private String cidr;
  private String gatewayIp;
  private String externalNetworkName;

  public Subnet() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
  }

  public String getCidr() {
    return cidr;
  }

  public void setCidr(String cidr) {
    this.cidr = cidr;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getGatewayIp() {
    return gatewayIp;
  }

  public void setGatewayIp(String gatewayIp) {
    this.gatewayIp = gatewayIp;
  }

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> dns;

  public List<String> getDns() {
    return dns;
  }

  public void setDns(List<String> dns) {
    this.dns = dns;
  }

  public String getExternalNetworkName() {
    return externalNetworkName;
  }

  public void setExternalNetworkName(String externalNetworkName) {
    this.externalNetworkName = externalNetworkName;
  }

  @Override
  public String toString() {
    return "Subnet{"
        + "name='"
        + name
        + '\''
        + ", extId='"
        + extId
        + '\''
        + ", networkId='"
        + networkId
        + '\''
        + ", cidr='"
        + cidr
        + '\''
        + ", gatewayIp='"
        + gatewayIp
        + '\''
        + ", externalNetworkName='"
        + externalNetworkName
        + '\''
        + "} "
        + super.toString();
  }
}
