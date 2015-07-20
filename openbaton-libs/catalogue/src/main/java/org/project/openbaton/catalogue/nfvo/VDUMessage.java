package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.common.Event;

import java.io.Serializable;

/**
 * Created by lto on 12/06/15.
 */
public class VDUMessage implements Serializable {

    private Event lifecycleEvent;
    private Serializable payload;

    public VDUMessage() {
    }

    public Event getLifecycleEvent() {
        return lifecycleEvent;
    }

    public void setLifecycleEvent(Event lifecycleEvent) {
        this.lifecycleEvent = lifecycleEvent;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "VDUMessage{" +
                "lifecycleEvent=" + lifecycleEvent +
                ", payload='" + payload + '\'' +
                '}';
    }
}
