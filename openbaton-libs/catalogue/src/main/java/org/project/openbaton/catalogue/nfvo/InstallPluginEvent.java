package org.project.openbaton.catalogue.nfvo;

import org.springframework.context.ApplicationEvent;

/**
 * Created by lto on 21/07/15.
 */
public class InstallPluginEvent extends ApplicationEvent {
    private String path;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public InstallPluginEvent(Object source) {
        super(source);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
