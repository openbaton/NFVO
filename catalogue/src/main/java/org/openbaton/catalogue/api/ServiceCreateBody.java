package org.openbaton.catalogue.api;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ServiceCreateBody {

  @NotNull
  @Size(min = 1)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NotNull
  @Size(min = 1)
  private List<String> roles;

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  @Override
  public String toString() {
    return "ServiceCreateBody{" + "name='" + name + '\'' + ", roles=" + roles + '}';
  }
}
