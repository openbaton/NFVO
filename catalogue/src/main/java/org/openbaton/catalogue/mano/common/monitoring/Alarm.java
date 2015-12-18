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

package org.openbaton.catalogue.mano.common.monitoring;

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
    private String triggerId;
    private String resourceId;

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

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
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
                ", triggerId='" + triggerId + '\'' +
                ", resourceId='" + resourceId + '\'' +
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
}
