package org.openbaton.monitoring.interfaces;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by lto on 15/10/15.
 */
public abstract class Monitoring extends UnicastRemoteObject implements ResourcePerformanceManagement {
    protected Monitoring() throws RemoteException {
    }
}
