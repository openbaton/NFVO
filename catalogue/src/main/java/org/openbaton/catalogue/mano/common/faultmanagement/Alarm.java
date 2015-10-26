package org.openbaton.catalogue.mano.common.faultmanagement;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.util.Date;
import java.util.List;

/**
 * Created by mob on 26.10.15.
 */
@Entity
public class Alarm {
    @Id
    private String alarmId;

    @ManyToOne
    private VirtualNetworkFunctionDescriptor vnfd;

    private Date alarmRaisedTime;
    private AlarmState alarmState;
    private PerceivedSeverity perceivedSeverity;
    private Date eventTime;
    private FaultType faultType;
    private String probableCause;
    private boolean isRootCause;
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

    public VirtualNetworkFunctionDescriptor getVnfd() {
        return vnfd;
    }

    public void setVnfd(VirtualNetworkFunctionDescriptor vnfd) {
        this.vnfd = vnfd;
    }

    public Date getAlarmRaisedTime() {
        return alarmRaisedTime;
    }

    public void setAlarmRaisedTime(Date alarmRaisedTime) {
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

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
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
                ", vnfd=" + vnfd +
                ", alarmRaisedTime=" + alarmRaisedTime +
                ", alarmState=" + alarmState +
                ", perceivedSeverity=" + perceivedSeverity +
                ", eventTime=" + eventTime +
                ", faultType=" + faultType +
                ", probableCause='" + probableCause + '\'' +
                ", isRootCause=" + isRootCause +
                ", correlatedAlarmId=" + correlatedAlarmId +
                ", faultDetails='" + faultDetails + '\'' +
                '}';
    }
}
