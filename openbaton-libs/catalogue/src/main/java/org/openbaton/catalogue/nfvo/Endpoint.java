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

/** Created by lto on 13/08/15. */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Endpoint implements Serializable {
  @Id protected String id;
  @Version protected int version = 0;

  protected String type;
  protected EndpointType endpointType;
  protected String endpoint;
  protected String description;
  protected boolean enabled;
  protected boolean active;

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return "Endpoint{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", type='"
        + type
        + '\''
        + ", endpointType="
        + endpointType
        + ", endpoint='"
        + endpoint
        + '\''
        + ", description='"
        + description
        + '\''
        + ", enabled="
        + enabled
        + ", active="
        + active
        + '}';
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public EndpointType getEndpointType() {
    return endpointType;
  }

  public void setEndpointType(EndpointType endpointType) {
    this.endpointType = endpointType;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
