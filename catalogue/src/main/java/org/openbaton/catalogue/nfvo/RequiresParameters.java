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

package org.openbaton.catalogue.nfvo;

import java.util.Iterator;
import java.util.Set;
import javax.persistence.*;
import org.openbaton.catalogue.util.IdGenerator;

/** Created by tbr on 07.07.16. */
@Entity(name = "requiresParameters")
public class RequiresParameters {

  @Id private String id;
  @Version private int version;

  @Column
  @ElementCollection(targetClass = String.class)
  private Set<String> parameters;

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<String> getParameters() {
    return parameters;
  }

  public void setParameters(Set<String> params) {
    this.parameters = params;
  }

  @Override
  public String toString() {
    return "requiresParameters{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", parameters=["
        + parametersToString()
        + ']'
        + '}';
  }

  private String parametersToString() {
    String returnString = "";
    for (Iterator<String> iterator = parameters.iterator(); iterator.hasNext(); ) {
      returnString += "\'" + iterator.next() + "\'";
      if (iterator.hasNext()) returnString += ", ";
    }
    return returnString;
  }
}
