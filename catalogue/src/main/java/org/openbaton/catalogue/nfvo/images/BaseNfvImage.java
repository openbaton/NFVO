package org.openbaton.catalogue.nfvo.images;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.openbaton.catalogue.util.BaseEntity;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class BaseNfvImage extends BaseEntity {
  protected String extId;

  @Temporal(TemporalType.TIMESTAMP)
  protected Date created;

  public String getExtId() {
    return extId;
  }

  public void setExtId(String extId) {
    this.extId = extId;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  @Override
  public String toString() {
    return "BaseNfvImage{"
        + "extId='"
        + extId
        + '\''
        + ", created="
        + created
        + "} "
        + super.toString();
  }
}
