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

package org.openbaton.catalogue.nfvo;

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 18/05/15. */
@Entity
public class Location extends BaseEntity {
  private String name;
  private String latitude;
  private String longitude;

  @Override
  public String toString() {
    return "Location{"
        + "name='"
        + name
        + '\''
        + ", latitude='"
        + latitude
        + '\''
        + ", longitude='"
        + longitude
        + '\''
        + "} "
        + super.toString();
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
}
