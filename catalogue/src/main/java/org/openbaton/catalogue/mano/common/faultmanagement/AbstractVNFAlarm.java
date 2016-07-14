/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 27.10.15.
 */
public abstract class AbstractVNFAlarm {
  private String resourceId, fmPolicyId;

  public AbstractVNFAlarm(String vnfrId, String faultManagementPolicyId) {
    this.resourceId = vnfrId;
    this.fmPolicyId = faultManagementPolicyId;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getFmPolicyId() {
    return fmPolicyId;
  }

  public void setFmPolicyId(String fmPolicyId) {
    this.fmPolicyId = fmPolicyId;
  }
}
