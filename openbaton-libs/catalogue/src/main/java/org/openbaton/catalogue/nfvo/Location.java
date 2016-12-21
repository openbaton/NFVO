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
import javax.persistence.*;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by lto on 18/05/15. */
@Entity
public class Location implements Serializable {
  @Id private String id;
  @Version private int version = 0;
  private String name;
  private String latitude;
  private String longitude;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
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

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  @Override
  public String toString() {
    return "Location{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", name='"
        + name
        + '\''
        + ", latitude='"
        + latitude
        + '\''
        + ", longitude='"
        + longitude
        + '\''
        + '}';
  }
}
