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
import java.util.Set;
import javax.persistence.*;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by lto on 20/05/15. */
@Entity
public class Network implements Serializable {
  @Id private String id;
  @Version private int version = 0;
  private String name;
  private String extId;
  private Boolean external = false;
  private Boolean shared = false;

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
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", extId='"
        + extId
        + '\''
        + ", external="
        + external
        + ", shared="
        + shared
        + ", subnets="
        + subnets
        + '}';
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getShared() {
    return shared;
  }

  public void setShared(Boolean shared) {
    this.shared = shared;
  }

  public Set<Subnet> getSubnets() {
    return subnets;
  }

  public void setSubnets(Set<Subnet> subnets) {
    this.subnets = subnets;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
}
