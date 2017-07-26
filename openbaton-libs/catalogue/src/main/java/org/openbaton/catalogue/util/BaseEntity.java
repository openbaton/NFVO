package org.openbaton.catalogue.util;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;
import javax.persistence.Version;

/** Created by lto on 04/04/2017. */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class BaseEntity implements Serializable {
  @Id private String id;

  @Version private int version;

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  private String projectId;

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

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  @Override
  public String toString() {
    return "BaseEntity{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", projectId='"
        + projectId
        + '\''
        + '}';
  }
}
