package org.project.neutrino.nfvo.vim;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.*;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.DeploymentFlavorManagement;
import org.project.neutrino.nfvo.vim_interfaces.ImageManagement;
import org.project.neutrino.nfvo.vim_interfaces.NetworkManagement;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class OpenstackVIM implements ImageManagement, ResourceManagement, NetworkManagement, DeploymentFlavorManagement {// TODO and so on...

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("openstackClient")
    private ClientInterfaces openstackClient;

    @Override
    public NFVImage add(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {
        openstackClient.init(vimInstance);
        try {
            NFVImage addedImage = openstackClient.addImage(image, inputStream);
            log.debug("Image with id: " + image.getId() + " added successfully.");
            return addedImage;
        } catch (Exception e) {
            log.warn("Image with id: " + image.getId() + " not added successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not added successfully.");
        }
    }

    @Override
    public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException{
        openstackClient.init(vimInstance);
        try {
            NFVImage updatedImage = openstackClient.updateImage(image);
            log.debug("Image with id: " + image.getId() + " updated successfully.");
            return updatedImage;
        } catch (Exception e) {
            log.error("Image with id: " + image.getId() + " not updated successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not updated successfully.");
        }
    }

    @Override
    public void copy(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException{
        openstackClient.init(vimInstance);
        try {
            openstackClient.copyImage(image, inputStream);
            log.debug("Image with id: " + image.getId() + " copied successfully.");
        } catch (Exception e) {
            log.error("Image with id: " + image.getId() + " not copied successfully.", e);
            throw new VimException("Image with id: " + image.getId() + " not copied successfully.");
        }
    }

    @Override
    public void delete(VimInstance vimInstance, NFVImage image) throws VimException {
        openstackClient.init(vimInstance);
        boolean isDeleted = openstackClient.deleteImage(image);
        if (isDeleted) {
            log.debug("Image with id: " + image.getId() + " deleted successfully.");
        } else {
            log.warn("Image with id: " + image.getId() + " not deleted successfully.");
            throw new VimException("Image with id: " + image.getId() + " not deleted successfully.");
        }
    }

    @Override
    public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        openstackClient.init(vimInstance);
        try {
            DeploymentFlavour flavor = openstackClient.addFlavor(deploymentFlavour);
            log.debug("Flavor with id: " + deploymentFlavour.getId() + " added successfully.");
            return flavor;
        } catch (Exception e) {
            log.error("Flavor with id: " + deploymentFlavour.getId() + " not added successfully.", e);
            throw new VimException("Image with id: " + deploymentFlavour.getId() + " not added successfully.");
        }
    }

    @Override
    public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        openstackClient.init(vimInstance);
        try {
            DeploymentFlavour flavor = openstackClient.updateFlavor(deploymentFlavour);
            log.debug("Flavor with id: " + deploymentFlavour.getId() + " updated successfully.");
            return flavor;
        } catch (Exception e) {
            log.error("Flavor with id: " + deploymentFlavour.getId() + " not updated successfully.", e);
            throw new VimException("Flavor with id: " + deploymentFlavour.getId() + " not updated successfully.");
        }
    }

    @Override
    public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException {
        openstackClient.init(vimInstance);
        boolean isDeleted = openstackClient.deleteFlavor(deploymentFlavor.getExtId());
        if (isDeleted) {
            log.debug("Flavor with id: " + deploymentFlavor.getId() + " deleted successfully.");
        } else {
            log.warn("Flavor with id: " + deploymentFlavor.getId() + " not deleted successfully.");
            throw new VimException("Flavor with id: " + deploymentFlavor.getId() + " not deleted successfully.");
        }
    }

    @Override
    public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) throws VimException {
        openstackClient.init(vimInstance);
        try {
            List<DeploymentFlavour> flavors = openstackClient.listFlavors();
            log.debug("Flavors listed successfully.");
            return flavors;
        } catch (Exception e) {
            log.error("Flavors not listed successfully.", e);
            throw new VimException("Flavors not listed successfully.");
        }
    }

    @Override
    public Network add(VimInstance vimInstance, Network network) throws VimException {
        openstackClient.init(vimInstance);
        Network createdNetwork = null;
        try {
            createdNetwork = openstackClient.createNetwork(network);
            log.debug("Network with id: " + network.getId() + " created successfully.");
        } catch (Exception e) {
            log.error("Network with id: " + network.getId() + " not created successfully.", e);
            throw new VimException("Network with id: " + network.getId() + " not deleted successfully.");
        }
        List<Subnet> createdSubnets = new ArrayList<Subnet>();
        for (Subnet subnet : network.getSubnets()) {
            try {
                Subnet createdSubnet = openstackClient.createSubnet(createdNetwork, subnet);
                log.debug("Subnet with id: " + subnet.getId() + " created successfully.");
                createdSubnet.setNetworkId(createdNetwork.getId());
                createdSubnets.add(createdSubnet);
            } catch (Exception e) {
                log.error("Subnet with id: " + subnet.getId() + " not created successfully.", e);
                throw new VimException("Subnet with id: " + subnet.getId() + " not created successfully.");
            }
        }
        createdNetwork.setSubnets(createdSubnets);
        return createdNetwork;
    }

    @Override
    public Network update(VimInstance vimInstance, Network network) throws VimException {
        openstackClient.init(vimInstance);
        Network updatedNetwork = null;
        try {
            updatedNetwork = openstackClient.updateNetwork(network);
        } catch (Exception e) {
            log.error("Network with id: " + network.getId() + " not updated successfully.", e);
            throw new VimException("Network with id: " + network.getId() + " not updated successfully.");
        }
        List<Subnet> updatedSubnets = new ArrayList<Subnet>();
        List<String> updatedSubnetExtIds = new ArrayList<String>();
        for (Subnet subnet : network.getSubnets()) {
            if (subnet.getExtId()!=null){
                try {
                    Subnet updatedSubnet = openstackClient.updateSubnet(updatedNetwork, subnet);
                    log.debug("Subnet with id: " + subnet.getId() + " updated successfully.");
                    updatedSubnet.setNetworkId(updatedNetwork.getId());
                    updatedSubnets.add(updatedSubnet);
                    updatedSubnetExtIds.add(updatedSubnet.getExtId());
                } catch (Exception e) {
                    log.error("Subnet with id: " + subnet.getId() + " not updated successfully.", e);
                    throw new VimException("Subnet with id: " + subnet.getId() + " not updated successfully.");
                }
            } else {
                try {
                    Subnet createdSubnet = openstackClient.createSubnet(updatedNetwork, subnet);
                    log.debug("Subnet with id: " + subnet.getId() + " created successfully.");
                    createdSubnet.setNetworkId(updatedNetwork.getId());
                    updatedSubnets.add(createdSubnet);
                    updatedSubnetExtIds.add(createdSubnet.getExtId());
                } catch (Exception e) {
                    log.error("Subnet with id: " + subnet.getId() + " not created successfully.", e);
                    throw new VimException("Subnet with id: " + subnet.getId() + " not created successfully.");
                }
            }
        }
        updatedNetwork.setSubnets(updatedSubnets);
        List<String> existingSubnetExtIds = openstackClient.getSubnetsExtIds(updatedNetwork.getExtId());
        for (String existingSubnetExtId : existingSubnetExtIds) {
            if (!updatedSubnetExtIds.contains(existingSubnetExtId)) {
                openstackClient.deleteSubnet(existingSubnetExtId);
            }
        }
        return updatedNetwork;
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException{
        openstackClient.init(vimInstance);
        boolean isDeleted = openstackClient.deleteNetwork(network.getExtId());
        if (isDeleted) {
            log.debug("Network with id: " + network.getId() + " deleted successfully.");
        } else {
            log.warn("Network with id: " + network.getId() + " not deleted successfully.");
            throw new VimException("Network with id: " + network.getId() + " not deleted successfully.");
        }
    }

    @Override
    public Network query(VimInstance vimInstance, String id) throws VimException {
        openstackClient.init(vimInstance);
        try {
            Network network = openstackClient.getNetworkById(id);
            log.debug("Network with id: " + network.getId() + " found.");
            return network;
        } catch (Exception e) {
            log.error("Network with id: " + id + "  not found.", e);
            throw new VimException("Network with id: " + id + "  not found.");
        }
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
        openstackClient.init(vimInstance);
        try {
            List<Network> networks = openstackClient.listNetworks();
            log.debug("Networks listed successfully.");
            return networks;
        } catch (Exception e) {
            log.error("Networks not listed successfully.", e);
            throw new VimException("Flavors not listed successfully.");
        }
    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord vnfr) throws VimException {
        VimInstance vimInstance = vdu.getVimInstance();
        log.trace("Initializing " + vimInstance);
        openstackClient.init(vimInstance);
        log.debug("initialized VimInstance");
        /**
         *  *) Create hostname
         *  *) choose image
         *  *) ...?
         */
        vdu.setHostname(vnfr.getName() + "-" + vdu.getId().substring((vdu.getId().length() - 5), vdu.getId().length() - 1));

        String image = this.chooseImage(vdu.getVm_image(), vimInstance);

        List<String> networks = new ArrayList<String>(){{add("7e62608d-efaa-4891-8ea2-3d3f537559c1");}};
        log.trace(""+vnfr);
        log.trace("");
        String flavorExtId = getFlavorExtID(vnfr.getDeployment_flavour_key(), vimInstance);
        log.trace("Params: " + vdu.getHostname() + " - " + image + " - " + flavorExtId + " - " + vimInstance.getKeyPair() + " - " + networks + " - " + vimInstance.getSecurityGroups());
        Server server = openstackClient.launchInstanceAndWait(vdu.getHostname(), image, flavorExtId, vimInstance.getKeyPair(), networks, vimInstance.getSecurityGroups(), "#userdata");
        log.debug("launched instance with id " + server.getExtId());
        vdu.setExtId(server.getExtId());
        return new AsyncResult<>(server.getExtId());
    }

    private String getFlavorExtID(String key, VimInstance vimInstance) throws VimException {
        openstackClient.init(vimInstance);
        for (DeploymentFlavour deploymentFlavour : vimInstance.getFlavours()){
            if (deploymentFlavour.getFlavour_key().equals(key) || deploymentFlavour.getExtId().equals(key) || deploymentFlavour.getId().equals(key)){
                return deploymentFlavour.getExtId();
            }
        }
        throw new VimException("no key " + key + " found in any vim instance found");
    }

    @Override
    public List<Server> queryResources(VimInstance vimInstance) {
        openstackClient.init(vimInstance);
        return openstackClient.listServer();
    }

//     TODO choose the right image (DONE)
    private String chooseImage(List<String> vm_images, VimInstance vimInstance) throws VimException {
        openstackClient.init(vimInstance);
        if (vm_images != null && vm_images.size() > 0) {
            for (String image : vm_images){
                for (NFVImage nfvImage : vimInstance.getImages()){
                    if (image.equals(nfvImage.getName()) || image.equals(nfvImage.getExtId()))
                        return nfvImage.getExtId();
                }
            }
            throw new VimException("No available image matching: " + vm_images);
        }
        throw new VimException("List of VM images is empty or null");
    }

    @Override
    public List<NFVImage> queryImages(VimInstance vimInstance) {
        openstackClient.init(vimInstance);
        return openstackClient.listImages();
    }

    @Override
    public void update(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void scale(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void migrate(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void operate(VirtualDeploymentUnit vdu, String operation) {

    }

    @Override
    public void release(VirtualDeploymentUnit vdu) {
        openstackClient.init(vdu.getVimInstance());
        openstackClient.deleteServerByIdAndWait(vdu.getExtId());
    }

    @Override
    public void createReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void queryReservation() {

    }

    @Override
    public void updateReservation(VirtualDeploymentUnit vdu) {

    }

    @Override
    public void releaseReservation(VirtualDeploymentUnit vdu) {

    }
}
