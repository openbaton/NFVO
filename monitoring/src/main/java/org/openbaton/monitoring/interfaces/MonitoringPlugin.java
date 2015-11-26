package org.openbaton.monitoring.interfaces;

import org.openbaton.plugin.interfaces.Plugin;

import java.rmi.RemoteException;

/**
 * Created by lto on 15/10/15.
 */
public abstract class MonitoringPlugin extends Plugin implements VirtualisedResourceFaultManagement,VirtualisedResourcesPerformanceManagement {

    protected MonitoringPlugin() throws RemoteException {
        super();
    }

}
