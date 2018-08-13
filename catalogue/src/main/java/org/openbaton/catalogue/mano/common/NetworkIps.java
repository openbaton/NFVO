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

package org.openbaton.catalogue.mano.common;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by ruthd on 02/03/18. */
@Entity
public class NetworkIps extends BaseEntity {
  private String netName;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<SubnetIp> subnetIps;

  public Set<SubnetIp> getSubnetIps() {
    return subnetIps;
  }

  public void setSubnetIps(Set<SubnetIp> subnetIps) {
    this.subnetIps = subnetIps;
  }

  public String getNetName() {
    return netName;
  }

  public void setNetName(String netName) {
    this.netName = netName;
  }

  public String printSubnetIps() {
    String retVal = "";
    for (SubnetIp subnetIp : subnetIps) {
      retVal +=
          "{'"
              + subnetIp.getIp()
              + "', '"
              + subnetIp.getSubnetName()
              + "', '"
              + subnetIp.getInterfaceId()
              + "'}";
    }
    return retVal;
  }

  @Override
  public String toString() {
    String retVal = "NetworkIps{" + "netName='" + netName + "\'";

    for (SubnetIp subnetIp : subnetIps) {
      retVal += ", " + subnetIp;
    }
    retVal += "} " + super.toString();
    return retVal;
  }
}
