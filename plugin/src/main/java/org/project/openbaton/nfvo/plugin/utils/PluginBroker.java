package org.project.openbaton.nfvo.plugin.utils;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by lto on 09/09/15.
 */
public class PluginBroker<T extends Remote> {

    public T getPlugin(String inte, String type) throws RemoteException, NotBoundException {
        Registry registry = getRegistry(1099);
        return lookupPlugin(inte, type, registry);
    }

    private T lookupPlugin(String inte, String type, Registry registry) throws RemoteException, NotBoundException {
        for (String name : registry.list()) {
            if (name.startsWith(inte + "." + type + ".")) {
                return (T) registry.lookup(name);
            }
        }
        throw new NotBoundException("plugin of type " + type + " not registered");
    }

    private Registry getRegistry(int port) throws RemoteException {
        return LocateRegistry.getRegistry(port);
    }

    public T getPlugin(String inte, String type, int port) throws RemoteException, NotBoundException {
        return lookupPlugin(inte, type, getRegistry(port));
    }

    public T getPlugin(String inte, String type, String name) throws RemoteException, NotBoundException {
        return (T) getRegistry(1099).lookup(inte + "." + type + "." + name);
    }

    public T getPlugin(String inte, String type, String name, int port) throws RemoteException, NotBoundException {

        return (T) getRegistry(port).lookup(inte + "." + type + "." + name);
    }
}
