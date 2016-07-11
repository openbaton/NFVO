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
