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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by lto on 01/07/15. */

/**
 * This class represents a Event Endpoint. When an external application want to receive events
 * regarding a particular entity it is necessary to send this object to the right URL or queue.
 *
 * <p>This object contains: * name) must be unique, used for removing a event * type)
 */
@Entity
public class EventEndpoint implements Serializable {
  @Id private String id;
  @Version private int version = 0;

  private String name;
  private String projectId;

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  private String networkServiceId;
  private String virtualNetworkFunctionId;

  private EndpointType type;
  private String endpoint;
  private Action event;
  private String description;
  private String status;

  @Override
  public String toString() {
    return "EventEndpoint{"
        + "description='"
        + description
        + '\''
        + ", id='"
        + id
        + '\''
        + ", version="
        + version
        + ", name='"
        + name
        + '\''
        + ", networkServiceId='"
        + networkServiceId
        + '\''
        + ", virtualNetworkFunctionId='"
        + virtualNetworkFunctionId
        + '\''
        + ", type="
        + type
        + ", endpoint='"
        + endpoint
        + '\''
        + ", event="
        + event
        + ", status='"
        + status
        + '\''
        + '}';
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNetworkServiceId() {
    return networkServiceId;
  }

  public void setNetworkServiceId(String networkServiceId) {
    this.networkServiceId = networkServiceId;
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getVirtualNetworkFunctionId() {
    return virtualNetworkFunctionId;
  }

  public void setVirtualNetworkFunctionId(String virtualNetworkFunctionId) {
    this.virtualNetworkFunctionId = virtualNetworkFunctionId;
  }

  public Action getEvent() {
    return event;
  }

  public void setEvent(Action event) {
    this.event = event;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EndpointType getType() {
    return type;
  }

  public void setType(EndpointType type) {
    this.type = type;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventEndpoint that = (EventEndpoint) o;

    if (!getName().equals(that.getName())) {
      return false;
    }
    if (!getProjectId().equals(that.getProjectId())) {
      return false;
    }
    if (getNetworkServiceId() != null
        ? !getNetworkServiceId().equals(that.getNetworkServiceId())
        : that.getNetworkServiceId() != null) {
      return false;
    }
    if (getVirtualNetworkFunctionId() != null
        ? !getVirtualNetworkFunctionId().equals(that.getVirtualNetworkFunctionId())
        : that.getVirtualNetworkFunctionId() != null) {
      return false;
    }
    if (getType() != that.getType()) {
      return false;
    }
    if (!getEndpoint().equals(that.getEndpoint())) {
      return false;
    }
    return getEvent() == that.getEvent();
  }

  @Override
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + getProjectId().hashCode();
    result = 31 * result + (getNetworkServiceId() != null ? getNetworkServiceId().hashCode() : 0);
    result =
        31 * result
            + (getVirtualNetworkFunctionId() != null
                ? getVirtualNetworkFunctionId().hashCode()
                : 0);
    result = 31 * result + getType().hashCode();
    result = 31 * result + getEndpoint().hashCode();
    result = 31 * result + getEvent().hashCode();
    return result;
  }
}
