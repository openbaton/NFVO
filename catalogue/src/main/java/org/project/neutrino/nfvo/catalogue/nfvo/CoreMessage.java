package org.project.neutrino.nfvo.catalogue.nfvo;

import java.io.Serializable;

/**
 * Created by lorenzo on 5/30/15.
 */
public class CoreMessage implements Serializable{
    private Action action;
    private Serializable object;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Serializable getObject() {
        return object;
    }

    public void setObject(Serializable object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "CoreMessage{" +
                "action=" + action +
                ", object=" + object +
                '}';
    }
}
