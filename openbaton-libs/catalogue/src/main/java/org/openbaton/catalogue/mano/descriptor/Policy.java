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

package org.openbaton.catalogue.mano.descriptor;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;
import org.openbaton.catalogue.util.IdGenerator;

/**
 * Created by lto on 06/02/15.
 *
 * <p>Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 *
 * <p>A policy or rule to apply to the NFP
 */
@Entity
public class Policy implements Serializable {

  @Id private String id;
  @Version private int version = 0;
  private ACL_Matching_Criteria acl_matching_criteria;
  private String qos_level;

  public Policy() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @PrePersist
  public void ensureId() {
    id = IdGenerator.createUUID();
  }

  public ACL_Matching_Criteria getMatchingCriteria() {
    return acl_matching_criteria;
  }

  public void setMatchingCriteria(ACL_Matching_Criteria matching_criteria) {
    this.acl_matching_criteria = matching_criteria;
  }

  public String getQoSLevel() {
    return qos_level;
  }

  public void setQoSLevel(String qos) {
    this.qos_level = qos;
  }
}
