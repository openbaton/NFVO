package org.project.neutrino.nfvo.catalogue.nfvo;

import java.io.Serializable;

/**
 * Created by lorenzo on 5/30/15.
 */
public class CoreMessage implements Serializable{
    private Action action;
    private Serializable payload;

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

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }
}
