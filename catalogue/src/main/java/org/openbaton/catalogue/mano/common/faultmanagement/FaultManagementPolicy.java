/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.catalogue.mano.common.faultmanagement;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.catalogue.util.BaseEntity;

/** Created by mob on 29.10.15. */
@Entity
public class FaultManagementPolicy extends BaseEntity {
  private String name;
  private Boolean isVNFAlarm = false;
  private int period;
  private FaultManagementAction action;
  private PerceivedSeverity severity;

  @OneToMany(
    cascade = {CascadeType.ALL},
    fetch = FetchType.EAGER
  )
  private Set<Criteria> criteria;

  public void setName(String name) {
    this.name = name;
  }

  public int getPeriod() {
    return period;
  }

  public void setPeriod(int period) {
    this.period = period;
  }

  public PerceivedSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(PerceivedSeverity severity) {
    this.severity = severity;
  }

  public Set<Criteria> getCriteria() {
    return criteria;
  }

  public void setCriteria(Set<Criteria> criteria) {
    this.criteria = criteria;
  }

  public String getName() {
    return name;
  }

  public Boolean isVNFAlarm() {
    return isVNFAlarm;
  }

  public Boolean getIsVNFAlarm() {
    return isVNFAlarm;
  }

  public void setIsVNFAlarm(Boolean isVNFAlarm) {
    this.isVNFAlarm = isVNFAlarm;
  }

  public FaultManagementAction getAction() {
    return action;
  }

  public void setAction(FaultManagementAction action) {
    this.action = action;
  }

  @Override
  public String toString() {
    return "FaultManagementPolicy{"
        + "name='"
        + name
        + '\''
        + ", isVNFAlarm="
        + isVNFAlarm
        + ", period="
        + period
        + ", action="
        + action
        + ", severity="
        + severity
        + ", criteria="
        + criteria
        + "} "
        + super.toString();
  }
}
