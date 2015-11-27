package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.mano.common.monitoring.Alarm;

/**
 * Created by mob on 27.10.15.
 */
public class VNFAlarmNotification extends AbstractVNFAlarm {
    private Alarm alarm;

    public VNFAlarmNotification(String vnfrId, String faultManagementPolicyId,Alarm alarm) {

        super(vnfrId,faultManagementPolicyId);
        this.alarm=alarm;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    @Override
    public String toString() {
        return "VNFAlarmNotification{" +
                "alarm=" + alarm +
                "} " + super.toString();
    }
}
