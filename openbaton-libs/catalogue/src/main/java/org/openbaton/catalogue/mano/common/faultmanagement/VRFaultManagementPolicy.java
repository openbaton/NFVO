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

import javax.persistence.Entity;

/**
 * Created by mob on 29.10.15.
 */
@Entity
public class VRFaultManagementPolicy extends FaultManagementPolicy {
  private FaultManagementAction action;

  public VRFaultManagementPolicy() {}

  public FaultManagementAction getAction() {
    return action;
  }

  public void setAction(FaultManagementAction action) {
    this.action = action;
  }

  @Override
  public String toString() {
    String result =
        "VRFaultManagementPolicy{"
            + "id='"
            + id
            + '\''
            + ", version="
            + version
            + ", name='"
            + name
            + '\''
            + ", period="
            + period
            + ", severity="
            + severity
            + ", criteria=";
    if (criteria != null) {
      for (Criteria aCriteria : criteria) {
        result += aCriteria.toString();
      }
    } else result += "null";
    result += ", action=" + action;
    return result;
  }
}
