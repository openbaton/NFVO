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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.*;

/** Created by rvl on 15.09.16. */
public class VNFAutoscaling {

  private Set<AutoScalePolicy> autoScalePolicySet;

  public VNFAutoscaling(Object o) {

    autoScalePolicySet = new HashSet<>();
    Map<String, Object> autoscalingMap = (Map<String, Object>) o;

    for (String key : autoscalingMap.keySet()) {
      Map<String, Object> policyMap = (Map<String, Object>) autoscalingMap.get(key);
      AutoScalePolicy asp = new AutoScalePolicy();
      asp.setName(key);

      if (policyMap.containsKey("threshold")) {
        asp.setThreshold((double) policyMap.get("threshold"));
      }
      if (policyMap.containsKey("comparisonOperator")) {
        asp.setComparisonOperator((String) policyMap.get("comparisonOperator"));
      }
      if (policyMap.containsKey("period")) {
        asp.setPeriod((Integer) policyMap.get("period"));
      }
      if (policyMap.containsKey("cooldown")) {
        asp.setCooldown((Integer) policyMap.get("cooldown"));
      }
      if (policyMap.containsKey("mode")) {
        asp.setMode(ScalingMode.valueOf((String) policyMap.get("mode")));
      }
      if (policyMap.containsKey("type")) {
        asp.setType(ScalingType.valueOf((String) policyMap.get("type")));
      }

      if (policyMap.containsKey("alarms")) {

        Map<String, Object> alarmsMap = (Map<String, Object>) policyMap.get("alarms");
        Set<ScalingAlarm> alarms = new HashSet<>();

        for (String alarmName : alarmsMap.keySet()) {
          ScalingAlarm alarm = new ScalingAlarm();
          Map<String, Object> alarmMap = (Map<String, Object>) alarmsMap.get(alarmName);

          if (alarmMap.containsKey("metric")) {
            alarm.setMetric((String) alarmMap.get("metric"));
          }
          if (alarmMap.containsKey("statistic")) {
            alarm.setStatistic((String) alarmMap.get("statistic"));
          }
          if (alarmMap.containsKey("comparisonOperator")) {
            alarm.setComparisonOperator((String) alarmMap.get("comparisonOperator"));
          }
          if (alarmMap.containsKey("threshold")) {
            alarm.setThreshold((double) alarmMap.get("threshold"));
          }
          if (alarmMap.containsKey("weight")) {
            alarm.setWeight((int) alarmMap.get("weight"));
          }
          alarms.add(alarm);
        }
        asp.setAlarms(alarms);
      }
      if (policyMap.containsKey("actions")) {

        Map<String, Object> actionsMap = (Map<String, Object>) policyMap.get("actions");
        Set<ScalingAction> actions = new HashSet<>();

        for (String actionsName : actionsMap.keySet()) {
          ScalingAction action = new ScalingAction();
          Map<String, Object> actionMap = (Map<String, Object>) actionsMap.get(actionsName);

          if (actionMap.containsKey("type")) {
            action.setType(ScalingActionType.valueOf((String) actionMap.get("type")));
          }
          if (actionMap.containsKey("target")) {
            action.setTarget((String) actionMap.get("target"));
          }
          if (actionMap.containsKey("value")) {
            action.setValue((String) actionMap.get("value"));
          }
          actions.add(action);
        }
        asp.setActions(actions);
      }

      autoScalePolicySet.add(asp);
    }
  }

  public Set<AutoScalePolicy> getAutoScalePolicySet() {
    return this.autoScalePolicySet;
  }
}
