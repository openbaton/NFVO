package org.openbaton.catalogue.nfvo;

import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 05/04/2017. */
@Entity
public class ManagerCredentials extends BaseEntity {
  private String rabbitUsername;
  private String rabbitPassword;

  public String getRabbitUsername() {
    return rabbitUsername;
  }

  public void setRabbitUsername(String rabbitUsername) {
    this.rabbitUsername = rabbitUsername;
  }

  public String getRabbitPassword() {
    return rabbitPassword;
  }

  public void setRabbitPassword(String rabbitPassword) {
    this.rabbitPassword = rabbitPassword;
  }

  @Override
  public String toString() {
    return "ManagerCredentials{"
        + "rabbitUsername='"
        + rabbitUsername
        + '\''
        + ", rabbitPassword='"
        + rabbitPassword
        + '\''
        + "} "
        + super.toString();
  }
}
