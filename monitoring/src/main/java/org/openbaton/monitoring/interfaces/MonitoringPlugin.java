package org.openbaton.monitoring.interfaces;

import org.openbaton.plugin.interfaces.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

/**
 * Created by lto on 15/10/15.
 */
public abstract class MonitoringPlugin extends Plugin implements ResourcePerformanceManagement {

    protected MonitoringPlugin() throws RemoteException {
        super();
    }

}
