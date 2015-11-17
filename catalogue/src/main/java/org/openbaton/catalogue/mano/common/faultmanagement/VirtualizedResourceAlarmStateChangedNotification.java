package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 28.10.15.
 */
public class VirtualizedResourceAlarmStateChangedNotification extends AbstractVirtualizedResourceAlarm {
    private AlarmState alarmState;

    public VirtualizedResourceAlarmStateChangedNotification(){}
    public VirtualizedResourceAlarmStateChangedNotification(String triggerId, AlarmState alarmState) {
        super(triggerId);
        this.alarmState=alarmState;

    }

    public AlarmState getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(AlarmState alarmState) {
        this.alarmState = alarmState;
    }

    @Override
    public String toString() {
        return "VirtualizedResourceAlarmStateChangedNotification{" +
                "alarmState=" + alarmState +
                "} " + super.toString();
    }
}
