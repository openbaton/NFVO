package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;

/**
 * Created by mob on 28.10.15.
 */
public class VNFAlarmStateChangedNotification extends AbstractVNFAlarm {
    private AlarmState alarmState;

    public VNFAlarmStateChangedNotification(String vnfrId, String faultManagementPolicyId,AlarmState alarmState) {
        super(vnfrId,faultManagementPolicyId);
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
        return "VNFAlarmStateChangedNotification{" +
                "alarmState=" + alarmState +
                "} " + super.toString();
    }
}
