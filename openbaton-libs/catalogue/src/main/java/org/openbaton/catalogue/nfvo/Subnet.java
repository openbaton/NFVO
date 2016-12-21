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

package org.openbaton.catalogue.nfvo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by mpa on 28.05.15. */
@Entity
public class Subnet implements Serializable {
  @Id private String id;
  @Version private int version = 0;
  private String name;
  private String extId;
  private String networkId;
  private String cidr;
  private String gatewayIp;

  public Subnet() {}

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

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

  @Override
  public String toString() {
    return "Subnet{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", name='"
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
        + '}';
  }
}
