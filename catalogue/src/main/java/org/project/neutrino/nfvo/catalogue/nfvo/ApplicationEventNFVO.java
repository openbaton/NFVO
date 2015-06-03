package org.project.neutrino.nfvo.catalogue.nfvo;

import org.springframework.context.ApplicationEvent;

/**
 * Created by lto on 03/06/15.
 */
public class ApplicationEventNFVO extends ApplicationEvent {
    private Action action;
    public ApplicationEventNFVO(Object source, Action action) {
        super(source);
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "ApplicationEventNFVO{" +
                "action=" + action +
                '}';
    }
}
