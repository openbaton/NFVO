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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;

/** Created by rvl on 06.02.17. */
public class VDUFaultManagement {

  private Set<FaultManagementPolicy> faultManagementPolicies;

  @SuppressWarnings({"unsafe", "unchecked"})
  public VDUFaultManagement(Object o) {
    faultManagementPolicies = new HashSet<>();

    Map<String, Object> fmMap = (Map<String, Object>) o;
    for (String key : fmMap.keySet()) {

      FaultManagementPolicy faultManagementPolicy = new FaultManagementPolicy();
      faultManagementPolicy.setName(key);
      Map<String, Object> policyMap = (Map<String, Object>) fmMap.get(key);

      if (policyMap.containsKey("isVNFAlarm")) {
        faultManagementPolicy.setIsVNFAlarm((Boolean) policyMap.get("isVNFAlarm"));
      }
      if (policyMap.containsKey("period")) {
        faultManagementPolicy.setPeriod((Integer) policyMap.get("period"));
      }
      if (policyMap.containsKey("severity")) {
        faultManagementPolicy.setSeverity(
            PerceivedSeverity.valueOf(((String) policyMap.get("severity")).toUpperCase()));
      }
      if (policyMap.containsKey("action")) {
        faultManagementPolicy.setAction(
            FaultManagementAction.valueOf(((String) policyMap.get("action")).toUpperCase()));
      }
      if (policyMap.containsKey("criteria")) {
        Set<Criteria> criterias = new HashSet<>();
        Map<String, Object> criteriaMap = (Map<String, Object>) policyMap.get("criteria");

        for (String criteriaName : criteriaMap.keySet()) {
          Criteria criteria = new Criteria();
          criteria.setName(criteriaName);
          Map<String, Object> map = (Map<String, Object>) criteriaMap.get(criteriaName);

          if (map.containsKey("parameter_ref")) {
            criteria.setParameter_ref((String) map.get("parameter_ref"));
          }
          if (map.containsKey("function")) {
            criteria.setFunction((String) map.get("function"));
          }
          if (map.containsKey("vnfc_selector")) {
            criteria.setVnfc_selector(VNFCSelector.valueOf((String) map.get("vnfc_selector")));
          }
          if (map.containsKey("comparison_operator")) {
            criteria.setComparison_operator((String) map.get("comparison_operator"));
          }
          if (map.containsKey("threshold")) {
            criteria.setThreshold((String) map.get("threshold"));
          }
          criterias.add(criteria);
        }

        faultManagementPolicy.setCriteria(criterias);
      }

      faultManagementPolicies.add(faultManagementPolicy);
    }
  }

  public Set<FaultManagementPolicy> getFaultManagementPolicies() {
    return faultManagementPolicies;
  }
}
