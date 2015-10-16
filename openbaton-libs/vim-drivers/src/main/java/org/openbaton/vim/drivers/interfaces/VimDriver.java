package org.openbaton.vim.drivers.interfaces;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by lto on 15/10/15.
 */
public abstract class VimDriver extends UnicastRemoteObject implements ClientInterfaces {
    protected VimDriver() throws RemoteException {
    }
}
