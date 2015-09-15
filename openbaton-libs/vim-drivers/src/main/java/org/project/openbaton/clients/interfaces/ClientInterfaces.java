package org.project.openbaton.clients.interfaces;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;

import java.io.InputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces extends Remote {

    /**
     * This version must match the version of the plugin...
     */
    String interfaceVersion = "1.0";

    Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair, Set<String> network, Set<String> secGroup, String userData) throws RemoteException;
//    void init(VimInstance vimInstance);

    List<NFVImage> listImages(VimInstance vimInstance) throws RemoteException;

    List<Server> listServer(VimInstance vimInstance) throws RemoteException;

    List<Network> listNetworks(VimInstance vimInstance) throws RemoteException;

    List<DeploymentFlavour> listFlavors(VimInstance vimInstance) throws RemoteException;

    Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws VimDriverException, RemoteException;

    void deleteServerByIdAndWait(VimInstance vimInstance, String id) throws RemoteException;

    Network createNetwork(VimInstance vimInstance, Network network) throws RemoteException;

    DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws RemoteException;

    NFVImage addImage(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws RemoteException;

    NFVImage updateImage(VimInstance vimInstance, NFVImage image) throws RemoteException;

    NFVImage copyImage(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws RemoteException;

    boolean deleteImage(VimInstance vimInstance, NFVImage image) throws RemoteException;

    DeploymentFlavour updateFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimDriverException, RemoteException;

    boolean deleteFlavor(VimInstance vimInstance, String extId) throws RemoteException;

    Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet) throws RemoteException;

    Network updateNetwork(VimInstance vimInstance, Network network) throws RemoteException;

    Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet) throws RemoteException;

    List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId) throws RemoteException;

    boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId) throws RemoteException;

    boolean deleteNetwork(VimInstance vimInstance, String extId) throws RemoteException;

    Network getNetworkById(VimInstance vimInstance, String id) throws RemoteException;

    Quota getQuota(VimInstance vimInstance) throws RemoteException;

    String getType(VimInstance vimInstance) throws RemoteException;
}
