package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.faultmanagement.Criteria;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementAction;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFCSelector;
import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;

/** Created by rvl on 06.02.17. */
public class VDUFaultManagement {

  private Set<VRFaultManagementPolicy> faultManagementPolicies;

  public VDUFaultManagement(Object o) {
    faultManagementPolicies = new HashSet<>();

    Map<String, Object> fmMap = (Map<String, Object>) o;
    for (String key : fmMap.keySet()) {

      VRFaultManagementPolicy vrFaultManagementPolicy = new VRFaultManagementPolicy();
      vrFaultManagementPolicy.setName(key);
      Map<String, Object> policyMap = (Map<String, Object>) fmMap.get(key);

      if (policyMap.containsKey("isVNFAlarm")) {
        vrFaultManagementPolicy.setVNFAlarm((Boolean) policyMap.get("isVNFAlarm"));
      }
      if (policyMap.containsKey("period")) {
        vrFaultManagementPolicy.setPeriod((Integer) policyMap.get("period"));
      }
      if (policyMap.containsKey("severity")) {
        vrFaultManagementPolicy.setSeverity(
            PerceivedSeverity.valueOf(((String) policyMap.get("severity")).toUpperCase()));
      }
      if (policyMap.containsKey("action")) {
        vrFaultManagementPolicy.setAction(
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

        vrFaultManagementPolicy.setCriteria(criterias);
      }

      faultManagementPolicies.add(vrFaultManagementPolicy);
    }
  }

  public Set<VRFaultManagementPolicy> getFaultManagementPolicies() {
    return faultManagementPolicies;
  }
}
