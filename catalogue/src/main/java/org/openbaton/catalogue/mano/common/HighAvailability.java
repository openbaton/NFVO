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

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;

/**
 * Created by lto on 06/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class HighAvailability {
  @Id private String id;
  @Version private int version = 0;
  private ResiliencyLevel resiliencyLevel;
  private boolean geoRedundancy;
  private String redundancyScheme;

  public HighAvailability() {}

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public ResiliencyLevel getResiliencyLevel() {
    return resiliencyLevel;
  }

  public void setResiliencyLevel(ResiliencyLevel resiliencyLevel) {
    this.resiliencyLevel = resiliencyLevel;
  }

  public boolean isGeoRedundancy() {
    return geoRedundancy;
  }

  public void setGeoRedundancy(boolean geoRedundancy) {
    this.geoRedundancy = geoRedundancy;
  }

  public String getRedundancyScheme() {
    return redundancyScheme;
  }

  public void setRedundancyScheme(String redundancyScheme) {
    this.redundancyScheme = redundancyScheme;
  }

  @Override
  public String toString() {
    return "HighAvailability{"
        + "resiliencyLevel="
        + resiliencyLevel
        + ", geoRedundancy="
        + geoRedundancy
        + ", redundancyScheme='"
        + redundancyScheme
        + '\''
        + '}';
  }
}
