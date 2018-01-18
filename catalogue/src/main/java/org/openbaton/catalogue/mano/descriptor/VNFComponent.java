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

package org.openbaton.catalogue.mano.descriptor;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import org.openbaton.catalogue.util.BaseEntity;

/** Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12) */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class VNFComponent extends BaseEntity {

  /**
   * Describes network connectivity between a VNFC instance (based on this VDU) and an internal
   * Virtual Link.
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @NotNull
  protected Set<VNFDConnectionPoint> connection_point;

  public VNFComponent() {
    this.connection_point = new HashSet<>();
  }

  @Override
  public String toString() {
    return "VNFComponent{" + "connection_point=" + connection_point + "} " + super.toString();
  }

  public Set<VNFDConnectionPoint> getConnection_point() {
    return connection_point;
  }

  public void setConnection_point(Set<VNFDConnectionPoint> connection_point) {
    this.connection_point = connection_point;
  }
}
