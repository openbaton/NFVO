package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

import java.io.Serializable;

/**
 * Created by lorenzo on 5/30/15.
 */
public class CoreMessage implements Serializable{
    private Action action;
    private VirtualNetworkFunctionRecord payload;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "CoreMessage{" +
                "action=" + action +
                ", payload=" + payload +
                '}';
    }

    public VirtualNetworkFunctionRecord getPayload() {
        return payload;
    }

    public void setPayload(VirtualNetworkFunctionRecord payload) {
        this.payload = payload;
    }
}
