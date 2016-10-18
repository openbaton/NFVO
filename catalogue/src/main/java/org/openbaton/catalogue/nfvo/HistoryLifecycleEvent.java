package org.openbaton.catalogue.nfvo;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;

/**
 * Created by lto on 18/10/16.
 */
@Entity
public class HistoryLifecycleEvent {

  @Id private String id;
  private String event;
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
