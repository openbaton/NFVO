package org.openbaton.catalogue.security;

import org.openbaton.catalogue.util.IdGenerator;

import java.text.DateFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;

/**
 * Created by lto on 17/10/16.
 */
@Entity
public class HistoryEntity {
  private String username;
  private String method;
  private String path;
  private String result;
  private long timestamp;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Id private String id;

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  @Override
  public String toString() {
    return "HistoryEntity{"
        + "username='"
        + username
        + '\''
        + ", method='"
        + method
        + '\''
        + ", path='"
        + path
        + '\''
        + ", result='"
        + result
        + '\''
        + ", timestamp="
        + DateFormat.getInstance().format(timestamp)
        + ", id='"
        + id
        + '\''
        + '}';
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getResult() {
    return result;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
