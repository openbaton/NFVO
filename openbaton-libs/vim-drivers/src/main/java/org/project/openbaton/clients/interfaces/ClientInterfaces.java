package org.project.openbaton.clients.interfaces;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces {

    /**
     * This version must match the version of the plugin...
     */
    String interfaceVersion = "1.0";
	
    Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair, Set<String> network, Set<String> secGroup, String userData);
//    void init(VimInstance vimInstance);

    List<NFVImage> listImages(VimInstance vimInstance);

    List<Server> listServer(VimInstance vimInstance);
    List<Network> listNetworks(VimInstance vimInstance);
    List<DeploymentFlavour> listFlavors(VimInstance vimInstance);

    Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws VimDriverException;

    void deleteServerByIdAndWait(VimInstance vimInstance, String id);
    Network createNetwork(VimInstance vimInstance, Network network);
    DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour);

    NFVImage addImage(VimInstance vimInstance, NFVImage image, InputStream inputStream);

    NFVImage updateImage(VimInstance vimInstance, NFVImage image);

    NFVImage copyImage(VimInstance vimInstance, NFVImage image, InputStream inputStream);

    boolean deleteImage(VimInstance vimInstance, NFVImage image);

    DeploymentFlavour updateFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimDriverException;

    boolean deleteFlavor(VimInstance vimInstance, String extId);

    Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet);

    Network updateNetwork(VimInstance vimInstance, Network network);

    Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet);

    List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId);

    boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId);

    boolean deleteNetwork(VimInstance vimInstance, String extId);

    Network getNetworkById(VimInstance vimInstance, String id);

    Quota getQuota(VimInstance vimInstance);

    String getType(VimInstance vimInstance);
}
