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

package org.openbaton.catalogue.mano.common.monitoring;

import javax.persistence.Entity;

/**
 * Created by mob on 21.01.16.
 */
@Entity
public class VRAlarm extends Alarm {

  //Actually is the hostname
  private String managedObject;

  public VRAlarm() {
    alarmType = AlarmType.VIRTUALIZED_RESOURCE;
  }

  @Override
  public AlarmType getAlarmType() {
    return alarmType;
  }

  @Override
  public void setAlarmType(AlarmType alarmType) {
    this.alarmType = alarmType;
  }

  public String getManagedObject() {
    return managedObject;
  }

  public void setManagedObject(String managedObject) {
    this.managedObject = managedObject;
  }

  @Override
  public String toString() {
    return "VRAlarm{" + "managedObject='" + managedObject + '\'' + "} " + super.toString();
  }
}
