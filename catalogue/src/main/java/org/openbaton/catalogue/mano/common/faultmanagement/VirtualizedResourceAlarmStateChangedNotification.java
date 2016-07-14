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

import org.openbaton.catalogue.mano.common.monitoring.AbstractVirtualizedResourceAlarm;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;

/**
 * Created by mob on 28.10.15.
 */
public class VirtualizedResourceAlarmStateChangedNotification
    extends AbstractVirtualizedResourceAlarm {
  private AlarmState alarmState;

  public VirtualizedResourceAlarmStateChangedNotification() {}

  public VirtualizedResourceAlarmStateChangedNotification(String triggerId, AlarmState alarmState) {
    super(triggerId);
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
    return "VirtualizedResourceAlarmStateChangedNotification{"
        + "alarmState="
        + alarmState
        + "} "
        + super.toString();
  }
}
