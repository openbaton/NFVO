package org.openbaton.catalogue.util;

import java.io.Serializable;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

  @Column(columnDefinition = "int default 0")
  @Version
  private Integer hbVersion = 0;

  public BaseEntity() {}

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  private String projectId;

  private boolean shared;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String, String> metadata;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getHbVersion() {
    return hbVersion;
  }

  public void setHbVersion(Integer hbVersion) {
    this.hbVersion = hbVersion;
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
        + hbVersion
        + ", projectId='"
        + projectId
        + '\''
        + ", shared="
        + shared
        + ", metadata="
        + metadata
        + '}';
  }

  public boolean isShared() {
    return shared;
  }

  public void setShared(boolean shared) {
    this.shared = shared;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}
