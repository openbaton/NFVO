package org.openbaton.catalogue.nfvo;

import javax.persistence.Column;
import javax.persistence.Entity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 04/04/2017. */
@Entity
public class ServiceMetadata extends BaseEntity {

  private String keyValue;

  @Column(unique = true)
  private String name;

  // The encrypted token
  private String token;

  private long tokenExpirationDate;

  public String getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(String keyValue) {
    this.keyValue = keyValue;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public long getTokenExpirationDate() {
    return tokenExpirationDate;
  }

  public void setTokenExpirationDate(long tokenExpirationDate) {
    this.tokenExpirationDate = tokenExpirationDate;
  }

  @Override
  public String toString() {
    return "ServiceMetadata{"
        + "key="
        + keyValue
        + ", name="
        + name
        + ", token="
        + token
        + ", tokenExpirationDate="
        + tokenExpirationDate
        + "} "
        + super.toString();
  }
}
