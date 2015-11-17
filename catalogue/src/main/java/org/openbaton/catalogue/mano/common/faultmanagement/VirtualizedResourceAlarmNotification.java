package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 27.10.15.
 */
public class VirtualizedResourceAlarmNotification extends AbstractVirtualizedResourceAlarm {
    private Alarm alarm;

    public VirtualizedResourceAlarmNotification(){

    }
    public VirtualizedResourceAlarmNotification(String triggerId, Alarm alarm) {
        super(triggerId);
        this.alarm=alarm;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    @Override
    public String toString() {
        return "VirtualizedResourceAlarmNotification{" +
                "alarm=" + alarm +
                "} " + super.toString();
    }
}
