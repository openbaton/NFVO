package org.openbaton.catalogue.security;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
public class BaseUser extends BaseEntity {

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  protected Set<Role> roles;

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "BaseUser{" + "roles=" + roles + "} " + super.toString();
  }
}
