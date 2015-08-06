package org.project.openbaton.nfvo.core.events;

import org.project.openbaton.catalogue.nfvo.InstallPluginEvent;
import org.project.openbaton.nfvo.core.core.PluginInstaller;
import org.project.openbaton.nfvo.exceptions.PluginInstallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by lto on 21/07/15.
 */
@Component
public class PluginInstallEventReceiver implements ApplicationListener<InstallPluginEvent> {

    @Autowired
    private PluginInstaller pluginInstaller;

    @Override
    public void onApplicationEvent(InstallPluginEvent event) {

        try {
            if (event.getType().equals("vim"))
                pluginInstaller.installVimDriverPlugin(event.getPath(), event.getClasses());
            else if (event.getType().equals("monitor"))
                pluginInstaller.installMonitoringPlugin(event.getPath(), event.getClasses());
        } catch (PluginInstallException e) {
            //TODO do something here
            e.printStackTrace();
        }
    }
}
