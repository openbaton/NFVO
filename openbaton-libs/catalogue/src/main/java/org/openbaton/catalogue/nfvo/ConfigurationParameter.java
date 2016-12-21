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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by lto on 18/05/15. */
@Entity
public class ConfigurationParameter implements Serializable {

  @Id private String id;
  @Version private int version;

  @Column(length = 1024)
  private String description;

  private String confKey;
  private String value;

  @Override
  public String toString() {
    return "ConfigurationParameter{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", confKey='"
        + confKey
        + '\''
        + ", value='"
        + value
        + '\''
        + ", description='"
        + description
        + '\''
        + '}';
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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
