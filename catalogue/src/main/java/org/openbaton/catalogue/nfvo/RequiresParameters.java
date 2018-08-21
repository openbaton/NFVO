/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.catalogue.nfvo;

import java.util.Iterator;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import org.openbaton.catalogue.util.BaseEntity;

@Entity(name = "requiresParameters")
public class RequiresParameters extends BaseEntity {

  @Column
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> parameters;

  public Set<String> getParameters() {
    return parameters;
  }

  public void setParameters(Set<String> params) {
    this.parameters = params;
  }

  @Override
  public String toString() {
    return "RequiresParameters{" + "parameters=" + parameters + "} " + super.toString();
  }

  private String parametersToString() {
    StringBuilder returnString = new StringBuilder();
    for (Iterator<String> iterator = parameters.iterator(); iterator.hasNext(); ) {
      returnString.append("\'").append(iterator.next()).append("\'");
      if (iterator.hasNext()) returnString.append(", ");
    }
    return returnString.toString();
  }
}
