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

import org.openbaton.catalogue.util.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

/** Created by lto on 18/05/15. */
@Entity
public class ConfigurationParameter extends BaseEntity {

  @Column(length = 1024)
  private String description;

  private String confKey;
  @Column(length = 1024)
  private String value;

  @Override
  public String toString() {
    return "ConfigurationParameter{"
        + "description='"
        + description
        + '\''
        + ", confKey='"
        + confKey
        + '\''
        + ", value='"
        + value
        + '\''
        + "} "
        + super.toString();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getConfKey() {
    return confKey;
  }

  public void setConfKey(String confKey) {
    this.confKey = confKey;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
