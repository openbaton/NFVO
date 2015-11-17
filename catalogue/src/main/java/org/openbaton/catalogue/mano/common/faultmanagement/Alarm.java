package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by mob on 26.10.15.
 */
@Entity
public class Alarm {
    @Id
    private String alarmId;

    private String resourceId;
    private String triggerId;

    private String alarmRaisedTime;
    private AlarmState alarmState;
    private PerceivedSeverity perceivedSeverity;
    private String eventTime;
    private FaultType faultType;
    private String probableCause;
    private boolean isRootCause;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> correlatedAlarmId;
    private String faultDetails;

    public Alarm() {
    }

    @PrePersist
    public void ensureId(){
        alarmId= IdGenerator.createUUID();
    }

    public String getAlarmId() {
        return alarmId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getAlarmRaisedTime() {
        return alarmRaisedTime;
    }

    public void setAlarmRaisedTime(String alarmRaisedTime) {
        this.alarmRaisedTime = alarmRaisedTime;
    }

    public AlarmState getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(AlarmState alarmState) {
        this.alarmState = alarmState;
    }

    public PerceivedSeverity getPerceivedSeverity() {
        return perceivedSeverity;
    }

    public void setPerceivedSeverity(PerceivedSeverity perceivedSeverity) {
        this.perceivedSeverity = perceivedSeverity;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public FaultType getFaultType() {
        return faultType;
    }

    public void setFaultType(FaultType faultType) {
        this.faultType = faultType;
    }

    public String getProbableCause() {
        return probableCause;
    }

    public void setProbableCause(String probableCause) {
        this.probableCause = probableCause;
    }

    public boolean isRootCause() {
        return isRootCause;
    }

    public void setIsRootCause(boolean isRootCause) {
        this.isRootCause = isRootCause;
    }

    public List<String> getCorrelatedAlarmId() {
        return correlatedAlarmId;
    }

    public void setCorrelatedAlarmId(List<String> correlatedAlarmId) {
        this.correlatedAlarmId = correlatedAlarmId;
    }

    public String getFaultDetails() {
        return faultDetails;
    }

    public void setFaultDetails(String faultDetails) {
        this.faultDetails = faultDetails;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "alarmId='" + alarmId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", triggerId='" + triggerId + '\'' +
                ", alarmRaisedTime='" + alarmRaisedTime + '\'' +
                ", alarmState=" + alarmState +
                ", perceivedSeverity=" + perceivedSeverity +
                ", eventTime='" + eventTime + '\'' +
                ", faultType=" + faultType +
                ", probableCause='" + probableCause + '\'' +
                ", isRootCause=" + isRootCause +
                ", correlatedAlarmId=" + correlatedAlarmId +
                ", faultDetails='" + faultDetails + '\'' +
                '}';
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

}
