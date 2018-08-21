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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import org.openbaton.catalogue.util.BaseEntity;

/**
 * Created by lto on 06/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class DeploymentFlavour extends BaseEntity {
  /*
   * Assurance parameter against which this flavour is being described. The key could be a combination of multiple
   * assurance
   * parameters with a logical relationship between them. The parameters should be present as a monitoring_parameter
   * supported in clause 6.2.1.1.
   * For example, a flavour of a virtual EPC could be described in terms of the assurance parameter "calls per
   * second" (cps).
   * */
  protected String flavour_key;

  protected String extId;
  private Integer ram = 0;
  private Integer disk = 0;
  private Integer vcpus = 0;

  public String getFlavour_key() {
    return flavour_key;
  }

  public void setFlavour_key(String flavour_key) {
    this.flavour_key = flavour_key;
  }

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
  }

  public Integer getRam() {
    return ram;
  }

  public void setRam(Integer ram) {
    this.ram = ram;
  }

  public Integer getDisk() {
    return disk;
  }

  public void setDisk(Integer disk) {
    this.disk = disk;
  }

  public Integer getVcpus() {
    return vcpus;
  }

  public void setVcpus(Integer vcpus) {
    this.vcpus = vcpus;
  }

  @Override
  public String toString() {
    return "DeploymentFlavour{"
        + "flavour_key='"
        + flavour_key
        + '\''
        + ", extId='"
        + extId
        + '\''
        + ", ram="
        + ram
        + ", disk="
        + disk
        + ", vcpus="
        + vcpus
        + "} "
        + super.toString();
  }
}
