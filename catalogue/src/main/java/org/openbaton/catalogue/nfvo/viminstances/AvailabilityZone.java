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

package org.openbaton.catalogue.nfvo.viminstances;

import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.validation.constraints.NotNull;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
public class AvailabilityZone extends BaseEntity {
  @NotNull private String name;

  private Boolean available = true;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, String> hosts;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getAvailable() {
    return available;
  }

  public void setAvailable(Boolean available) {
    this.available = available;
  }

  public Map<String, String> getHosts() {
    return hosts;
  }

  public void setHosts(Map<String, String> hosts) {
    this.hosts = hosts;
  }

  @Override
  public String toString() {
    return "AvailabilityZone{"
        + "name='"
        + name
        + '\''
        + ", available="
        + available
        + ", hosts="
        + hosts
        + "} "
        + super.toString();
  }
}
