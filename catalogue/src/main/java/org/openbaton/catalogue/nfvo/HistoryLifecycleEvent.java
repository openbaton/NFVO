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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by lto on 18/10/16. */
@Entity
public class HistoryLifecycleEvent {

  @Id private String id;
  private String event;

  @Column(length = 1024)
  private String description;

  private String executedAt;

  @Override
  public String toString() {
    return "HistoryLifecycleEvent{"
        + "id='"
        + id
        + '\''
        + ", event='"
        + event
        + '\''
        + ", description='"
        + description
        + '\''
        + ", executedAt='"
        + executedAt
        + '\''
        + '}';
  }

  public String getExecutedAt() {
    return executedAt;
  }

  public void setExecutedAt(String executedAt) {
    this.executedAt = executedAt;
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

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
