package org.project.openbaton.nfvo.common.interfaces;

import org.project.openbaton.catalogue.nfvo.PluginEndpoint;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.PluginInvokeException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.lang.reflect.Method;

/**
 * Created by tce on 13.08.15.
 */
public abstract class PluginAgent {

    public abstract <T> T invokeMethod(Method method, Class inter, Object... parameters) throws NotFoundException, PluginInvokeException;

    public abstract void register(PluginEndpoint endpoint);

    @JmsListener(destination = "plugin-register", containerFactory = "queueJmsContainerFactory")
    public void addManagerEndpoint(@Payload PluginEndpoint endpoint) {
        this.register(endpoint);
    }
}
