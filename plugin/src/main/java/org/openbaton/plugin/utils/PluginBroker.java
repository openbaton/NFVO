/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.plugin.utils;

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
