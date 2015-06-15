package org.project.neutrino.nfvo.vim.client.test;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.nfvo.*;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope
public class TestClient implements ClientInterfaces {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Server launchInstance(String name, String image, String flavor, String keypair, List<String> network, List<String> secGroup, String userData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init(VimInstance vimInstance) {
        log.debug("Initilizing testClient");
    }

    @Override
    public List<NFVImage> listImages() {
        return new ArrayList<NFVImage>(){{
            NFVImage image = new NFVImage();
            image.setExtId("ext_id_1");
            image.setName("ubuntu-14.04-server-cloudimg-amd64-disk1");
            add(image);

            image = new NFVImage();
            image.setExtId("ext_id_2");
            image.setName("image_name_1");
            add(image);
        }};
    }

    @Override
    public List<Server> listServer() {
        return new ArrayList<Server>(){{
            Server server = new Server();
            server.setName("server_name");
            server.setExtId("ext_id");
            add(server);
        }};

    }

    @Override
    public List<Network> listNetworks() {
        return new ArrayList<Network>(){{
            Network network = new Network();
            network.setExtId("ext_id");
            network.setName("network_name");
            add(network);
        }};
    }

    @Override
    public List<DeploymentFlavour> listFlavors() {
        return new ArrayList<DeploymentFlavour>(){{
            DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
            deploymentFlavour.setExtId("ext_id_1");
            deploymentFlavour.setFlavour_key("flavor_name");
            add(deploymentFlavour);

            deploymentFlavour = new DeploymentFlavour();
            deploymentFlavour.setExtId("ext_id_2");
            deploymentFlavour.setFlavour_key("m1.tiny");
            add(deploymentFlavour);

            deploymentFlavour = new DeploymentFlavour();
            deploymentFlavour.setExtId("ext_id_3");
            deploymentFlavour.setFlavour_key("m1.small");
            add(deploymentFlavour);
        }};
    }

    @Override
    public Server launchInstanceAndWait(String hostname, String image, String extId, String keyPair, List<String> networks, List<String> securityGroups, String s) {
        try {
            Thread.sleep((long) (Math.random() * 3500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Server server = new Server();
        server.setName("server_name");
        server.setExtId("ext_id");
        return server;
    }

    @Override
    public void deleteServerByIdAndWait(String id) {
        try {
            Thread.sleep((long) (Math.random() * 1500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Network createNetwork(Network network) {
        return network;
    }

    @Override
    public DeploymentFlavour addFlavor(DeploymentFlavour deploymentFlavour) {
        return deploymentFlavour;
    }

    @Override
    public NFVImage addImage(NFVImage image, InputStream inputStream) {
        return image;
    }

    @Override
    public NFVImage updateImage(NFVImage image) {
        return image;
    }

    @Override
    public NFVImage copyImage(NFVImage image, InputStream inputStream) {
        return image;
    }

    @Override
    public boolean deleteImage(NFVImage image) {
        return true;
    }

    @Override
    public DeploymentFlavour updateFlavor(DeploymentFlavour deploymentFlavour) {
        return deploymentFlavour;
    }

    @Override
    public boolean deleteFlavor(String extId) {
        return true;
    }

    @Override
    public Subnet createSubnet(Network createdNetwork, Subnet subnet) {
        return subnet;
    }

    @Override
    public Network updateNetwork(Network network) {
        return network;
    }

    @Override
    public Subnet updateSubnet(Network updatedNetwork, Subnet subnet) {
        return subnet;
    }

    @Override
    public List<String> getSubnetsExtIds(String network_extId) {
        return null;
    }

    @Override
    public boolean deleteSubnet(String existingSubnetExtId) {
        return false;
    }

    @Override
    public boolean deleteNetwork(String extId) {
        return false;
    }

    @Override
    public Network getNetworkById(String id) {
        return null;
    }

    @Override
    public Quota getQuota() {
        throw new UnsupportedOperationException();
    }
}
