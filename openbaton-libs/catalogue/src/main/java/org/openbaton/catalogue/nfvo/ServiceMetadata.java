package org.openbaton.catalogue.nfvo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by lto on 04/04/2017. */
@Entity
public class ServiceMetadata extends BaseEntity {

  //  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @Lob private byte[] keyValue;

  @Column(unique = true)
  private String name;

  private String status;

  public byte[] getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(byte[] keyValue) {
    this.keyValue = keyValue;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ServiceMetadata{"
        + "key="
        + keyValue
        + ", name='"
        + name
        + '\''
        + "} "
        + super.toString();
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
