package org.project.openbaton.clients.interfaces;

import org.project.openbaton.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.nfvo.catalogue.nfvo.*;
import org.project.openbaton.nfvo.common.exceptions.VimException;

import java.io.InputStream;
import java.util.List;

/**
 * Created by lto on 12/05/15.
 */
public interface ClientInterfaces {

    public Server launchInstance(String name, String image, String flavor, String keypair, List<String> network, List<String> secGroup, String userData);
    public void init(VimInstance vimInstance);

    List<NFVImage> listImages();

    List<Server> listServer();
    List<Network> listNetworks();
    List<DeploymentFlavour> listFlavors();

    Server launchInstanceAndWait(String hostname, String image, String extId, String keyPair, List<String> networks, List<String> securityGroups, String s) throws VimException;

    void deleteServerByIdAndWait(String id);
    Network createNetwork(Network network);
    DeploymentFlavour addFlavor(DeploymentFlavour deploymentFlavour);

    NFVImage addImage(NFVImage image, InputStream inputStream);

    NFVImage updateImage(NFVImage image);

    NFVImage copyImage(NFVImage image, InputStream inputStream);

    boolean deleteImage(NFVImage image);

    DeploymentFlavour updateFlavor(DeploymentFlavour deploymentFlavour) throws VimException;

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
