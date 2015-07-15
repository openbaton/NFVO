package org.project.openbaton.clients.interfaces;

import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;


import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces {

    public Server launchInstance(String name, String image, String flavor, String keypair, Set<String> network, Set<String> secGroup, String userData);
    public void init(VimInstance vimInstance);

    List<NFVImage> listImages();

    List<Server> listServer();
    List<Network> listNetworks();
    List<DeploymentFlavour> listFlavors();

    Server launchInstanceAndWait(String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws VimDriverException;

    void deleteServerByIdAndWait(String id);
    Network createNetwork(Network network);
    DeploymentFlavour addFlavor(DeploymentFlavour deploymentFlavour);

    NFVImage addImage(NFVImage image, InputStream inputStream);

    NFVImage updateImage(NFVImage image);

    NFVImage copyImage(NFVImage image, InputStream inputStream);

    boolean deleteImage(NFVImage image);

    DeploymentFlavour updateFlavor(DeploymentFlavour deploymentFlavour) throws VimDriverException;

    boolean deleteFlavor(String extId);

    Subnet createSubnet(Network createdNetwork, Subnet subnet);

    Network updateNetwork(Network network);

    Subnet updateSubnet(Network updatedNetwork, Subnet subnet);

    List<String> getSubnetsExtIds(String network_extId);

    boolean deleteSubnet(String existingSubnetExtId);

    boolean deleteNetwork(String extId);

    Network getNetworkById(String id);

    Quota getQuota();
}
