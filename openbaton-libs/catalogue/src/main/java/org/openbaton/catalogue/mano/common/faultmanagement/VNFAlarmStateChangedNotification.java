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

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;

/**
 * Created by mob on 28.10.15.
 */
public class VNFAlarmStateChangedNotification extends AbstractVNFAlarm {
  private AlarmState alarmState;

  public VNFAlarmStateChangedNotification(
      String vnfrId, String faultManagementPolicyId, AlarmState alarmState) {
    super(vnfrId, faultManagementPolicyId);
    this.alarmState = alarmState;
  }

  public AlarmState getAlarmState() {
    return alarmState;
  }

  public void setAlarmState(AlarmState alarmState) {
    this.alarmState = alarmState;
  }

  @Override
  public String toString() {
    return "VNFAlarmStateChangedNotification{"
        + "alarmState="
        + alarmState
        + "} "
        + super.toString();
  }
}
