package org.project.openbaton.catalogue.nfvo;

import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Created by lto on 21/07/15.
 */
public class InstallPluginEvent extends ApplicationEvent {
    private String path;
    private String type;
    private List<String> classes;

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

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
}
