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

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by lto on 21/08/15.
 */
@Entity
public class VNFCDependencyParameters implements Serializable {
  private String vnfcId;
  @Id private String id;
  @Version private int version = 0;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Map<String, DependencyParameters> parameters;

  public String getVnfcId() {
    return vnfcId;
  }

  public void setVnfcId(String vnfcId) {
    this.vnfcId = vnfcId;
  }

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

  public Map<String, DependencyParameters> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, DependencyParameters> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return "VNFCDependencyParameters{"
        + "id='"
        + id
        + '\''
        + ", vnfcId='"
        + vnfcId
        + '\''
        + ", version="
        + version
        + ", parameters="
        + parameters
        + '}';
  }
}
