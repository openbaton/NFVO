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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.openbaton.catalogue.util.BaseEntity;

/**
 * This class represents a Event Endpoint. When an external application want to receive events
 * regarding a particular entity it is necessary to send this object to the right URL or queue.
 *
 * <p>This object contains: * name) must be unique, used for removing a event * type)
 */
@Entity
public class EventEndpoint extends BaseEntity {

  @NotNull
  @Size(min = 1)
  private String name;

  private String networkServiceId;
  private String virtualNetworkFunctionId;

  @NotNull private EndpointType type;

  @NotNull
  @Size(min = 1)
  private String endpoint;

  @NotNull private Action event;

  private String description;
  private String status;

  @Override
  public String toString() {
    return "EventEndpoint{"
        + "name='"
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
        + ", description='"
        + description
        + '\''
        + ", status='"
        + status
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
}
