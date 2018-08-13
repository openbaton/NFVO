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

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by ruthd on 02/03/18. */
@Entity
public class SubnetIp extends BaseEntity {

  // There can be more than one instance of SubnetIp with the same subnet name associated with a single server on
  // a single network.  (NOTE: in aws the subnetName is empty)
  private String subnetName;
  private String ip;
  // The interfaceId will be the interface id from the vnfd.json file if provided (0 - n), otherwise it will be empty
  private String interfaceId = "";

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getSubnetName() {
    return subnetName;
  }

  public void setSubnetName(String subnetName) {
    this.subnetName = subnetName;
  }

  public String getInterfaceId() {
    return interfaceId;
  }

  public void setInterfaceId(String interfaceId) {
    this.interfaceId = interfaceId;
  }

  @Override
  public String toString() {
    return "SubnetIp{"
        + "subnetName='"
        + subnetName
        + '\''
        + ", ip='"
        + ip
        + '\''
        + ", interfaceId='"
        + interfaceId
        + '\''
        + "} "
        + super.toString();
  }
}
