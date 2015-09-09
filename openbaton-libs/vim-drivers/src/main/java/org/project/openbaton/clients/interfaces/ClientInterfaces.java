package org.project.openbaton.clients.interfaces;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;

import java.io.InputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 12/05/15.
 */
public abstract class ClientInterfaces extends UnicastRemoteObject implements Remote {

    /**
     * This version must match the version of the plugin...
     */
    public static final String interfaceVersion = "1.0";

    public ClientInterfaces() throws RemoteException {
    }

    public abstract Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair, Set<String> network, Set<String> secGroup, String userData) throws RemoteException;
//    void init(VimInstance vimInstance);

    public abstract List<NFVImage> listImages(VimInstance vimInstance) throws RemoteException;

    public abstract List<Server> listServer(VimInstance vimInstance) throws RemoteException;

    public abstract List<Network> listNetworks(VimInstance vimInstance) throws RemoteException;

    public abstract List<DeploymentFlavour> listFlavors(VimInstance vimInstance) throws RemoteException;

    public abstract Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws VimDriverException, RemoteException;

    public abstract void deleteServerByIdAndWait(VimInstance vimInstance, String id) throws RemoteException;

    public abstract Network createNetwork(VimInstance vimInstance, Network network) throws RemoteException;

    public abstract DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws RemoteException;

    public abstract NFVImage addImage(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws RemoteException;

    public abstract NFVImage updateImage(VimInstance vimInstance, NFVImage image) throws RemoteException;

    public abstract NFVImage copyImage(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws RemoteException;

    public abstract boolean deleteImage(VimInstance vimInstance, NFVImage image) throws RemoteException;

    public abstract DeploymentFlavour updateFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimDriverException, RemoteException;

    public abstract boolean deleteFlavor(VimInstance vimInstance, String extId) throws RemoteException;

    public abstract Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet) throws RemoteException;

    public abstract Network updateNetwork(VimInstance vimInstance, Network network) throws RemoteException;

    public abstract Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet) throws RemoteException;

    public abstract List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId) throws RemoteException;

    public abstract boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId) throws RemoteException;

    public abstract boolean deleteNetwork(VimInstance vimInstance, String extId) throws RemoteException;

    public abstract Network getNetworkById(VimInstance vimInstance, String id) throws RemoteException;

    public abstract Quota getQuota(VimInstance vimInstance) throws RemoteException;

    public abstract String getType(VimInstance vimInstance) throws RemoteException;
}
