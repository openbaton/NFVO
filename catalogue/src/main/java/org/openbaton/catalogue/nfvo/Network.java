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

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 20/05/15. */
@Entity
public class Network extends BaseEntity {
  private String name;
  private String extId;
  private Boolean external = false;
  private Boolean extShared = false;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<Subnet> subnets;

  public Network() {}

  public Boolean getExternal() {
    return external;
  }

  public void setExternal(Boolean external) {
    this.external = external;
  }

  @Override
  public String toString() {
    return "Network{"
        + "name='"
        + name
        + '\''
        + ", extId='"
        + extId
        + '\''
        + ", external="
        + external
        + ", extShared="
        + extShared
        + ", subnets="
        + subnets
        + "} "
        + super.toString();
  }

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getExtShared() {
    return extShared;
  }

  public void setExtShared(Boolean extShared) {
    this.extShared = extShared;
  }

  public Set<Subnet> getSubnets() {
    return subnets;
  }

  public void setSubnets(Set<Subnet> subnets) {
    this.subnets = subnets;
  }
}
