package org.openbaton.catalogue.security;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lto on 24/05/16.
 */
@Entity
public class Role implements Serializable {
  @Id private String id;

  @Enumerated(EnumType.STRING)
  private RoleEnum role;

  private String project;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  @Override
  public String toString() {
    return "Role{" + "role=" + role + ", project=" + project + '}';
  }

  public RoleEnum getRole() {
    return role;
  }

  public void setRole(RoleEnum role) {
    this.role = role;
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public enum RoleEnum {
    GUEST,
    ADMIN,
    OB_ADMIN,
  }
}
