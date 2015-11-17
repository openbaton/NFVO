package org.openbaton.catalogue.mano.common.faultmanagement;

/**
 * Created by mob on 27.10.15.
 */
public abstract class AbstractVirtualizedResourceAlarm {
    private String triggerId;

    public AbstractVirtualizedResourceAlarm(){

    }
    public AbstractVirtualizedResourceAlarm(String triggerId) {
        this.triggerId =triggerId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }
}
