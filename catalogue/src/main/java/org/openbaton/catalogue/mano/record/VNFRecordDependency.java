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

package org.openbaton.catalogue.mano.record;

import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.nfvo.DependencyParameters;
import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 08/06/15. */
@Entity
public class VNFRecordDependency extends BaseEntity {

  private String target;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Map<String, DependencyParameters> parameters;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Map<String, VNFCDependencyParameters> vnfcParameters;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, String> idType;

  public VNFRecordDependency() {}

  public Map<String, VNFCDependencyParameters> getVnfcParameters() {
    return vnfcParameters;
  }

  public void setVnfcParameters(Map<String, VNFCDependencyParameters> vnfcParameters) {
    this.vnfcParameters = vnfcParameters;
  }

  public Map<String, String> getIdType() {
    return idType;
  }

  public void setIdType(Map<String, String> idType) {
    this.idType = idType;
  }

  public Map<String, DependencyParameters> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, DependencyParameters> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return "VNFRecordDependency{"
        + "target='"
        + target
        + '\''
        + ", parameters="
        + parameters
        + ", vnfcParameters="
        + vnfcParameters
        + ", idType="
        + idType
        + "} "
        + super.toString();
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }
}
