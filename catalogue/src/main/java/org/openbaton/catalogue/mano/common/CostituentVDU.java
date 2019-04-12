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

package org.openbaton.catalogue.mano.common;

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/**
 * Created by lto on 06/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class CostituentVDU extends BaseEntity {
  /**
   * References a VDU which should be used for this deployment flavour by vnfd:vdu:id, see clause
   * 6.3.1.2.1.
   */
  private String vdu_reference;
  /** Number of VDU instances required */
  private int number_of_instances;
  /**
   * References VNFCs which should be used for this deployment flavour by vnfd:vdu:vnfc:id TODO
   * understand what is a VNF component
   */
  private String constituent_vnfc;

  public CostituentVDU() {}

  @Override
  public String toString() {
    return "CostituentVDU{"
        + "vdu_reference='"
        + vdu_reference
        + '\''
        + ", number_of_instances="
        + number_of_instances
        + ", constituent_vnfc='"
        + constituent_vnfc
        + '\''
        + "} "
        + super.toString();
  }

  public String getVdu_reference() {
    return vdu_reference;
  }

  public void setVdu_reference(String vdu_reference) {
    this.vdu_reference = vdu_reference;
  }

  public int getNumber_of_instances() {
    return number_of_instances;
  }

  public void setNumber_of_instances(int number_of_instances) {
    this.number_of_instances = number_of_instances;
  }

  public String getConstituent_vnfc() {
    return constituent_vnfc;
  }

  public void setConstituent_vnfc(String constituent_vnfc) {
    this.constituent_vnfc = constituent_vnfc;
  }
}
