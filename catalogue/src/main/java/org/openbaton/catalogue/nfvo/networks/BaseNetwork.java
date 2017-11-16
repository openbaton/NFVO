package org.openbaton.catalogue.nfvo.networks;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class BaseNetwork extends BaseEntity {
  protected String name;
  protected String extId;

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "BaseNetwork{"
        + "name='"
        + name
        + '\''
        + ", extId='"
        + extId
        + '\''
        + "} "
        + super.toString();
  }
}
