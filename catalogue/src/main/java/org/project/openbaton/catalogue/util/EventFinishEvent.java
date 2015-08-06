package org.project.openbaton.catalogue.util;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.springframework.context.ApplicationEvent;

/**
 * Created by lto on 06/08/15.
 */
public class EventFinishEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public EventFinishEvent(Object source) {
        super(source);
    }

    private Action action;
    private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
        return virtualNetworkFunctionRecord;
    }

    public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
    }

    @Override
    public String toString() {
        return "EventFinishEvent{" +
                "action=" + action +
                ", virtualNetworkFunctionRecord=" + virtualNetworkFunctionRecord +
                '}';
    }
}
