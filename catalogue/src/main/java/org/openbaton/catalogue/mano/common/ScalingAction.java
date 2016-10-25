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

package org.openbaton.catalogue.mano.common;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by mpa on 15/12/15.
 */
@Entity
public class ScalingAction implements Serializable {
  @Id private String id;
  @Version private int version = 0;

  @Enumerated(EnumType.STRING)
  private ScalingActionType type;

  private String value;

  private String target;

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public String getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public ScalingActionType getType() {
    return type;
  }

  public void setType(ScalingActionType type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  @Override
  public String toString() {
    return "ScalingAction{"
        + "id='"
        + id
        + '\''
        + ", version="
        + version
        + ", type="
        + type
        + ", value='"
        + value
        + '\''
        + ", target='"
        + target
        + '\''
        + '}';
  }
}
