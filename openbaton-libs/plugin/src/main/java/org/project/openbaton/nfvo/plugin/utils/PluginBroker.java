package org.project.openbaton.nfvo.plugin.utils;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by lto on 09/09/15.
 */
public class PluginBroker<T extends Remote> {

    public T getPlugin(String name) throws RemoteException, NotBoundException {
        return (T) LocateRegistry.getRegistry(1099).lookup(name);
    }
    public T getPlugin(String name, int port) throws RemoteException, NotBoundException {
        return (T) LocateRegistry.getRegistry(port).lookup(name);
    }
}
