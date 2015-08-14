package org.project.openbaton.nfvo.plugin.concretes;

import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.nfvo.Item;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.PluginInvokeException;
import org.project.openbaton.nfvo.plugin.PluginAgent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Created by lto on 14/08/15.
 */
@Service
@Scope
public class ResourcePerformanceManagementPluginAgent extends PluginAgent {

    public Item getMeasurementResults(String type, VirtualDeploymentUnit virtualDeploymentUnit, String metric, String period) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class,type, virtualDeploymentUnit,metric,period);
    }

    public Void notifyResults(String type) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type);
    }
}
