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

package org.openbaton.catalogue.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 24/05/16. */
@Entity
public class Project extends BaseEntity {

  @Column(unique = true)
  private String name;

  private String description;

  private Quota quota;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Project{"
        + "name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", quota="
        + quota
        + "} "
        + super.toString();
  }

  public Quota getQuota() {
    return quota;
  }

  public void setQuota(Quota quota) {
    this.quota = quota;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
