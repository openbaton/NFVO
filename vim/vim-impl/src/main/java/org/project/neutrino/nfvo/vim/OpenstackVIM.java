package org.project.neutrino.nfvo.vim;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim.client.openstack.OpenstackClient;
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
    private OpenstackClient openstackClient;

    @Override
    public NFVImage add(NFVImage image) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Network add(Network network) {
        Network createdNetwork = openstackClient.createNetwork(network);
        return createdNetwork;
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Network update(Network new_network, String id) {
        Network updatedNetwork = openstackClient.updateNetwork(id, new_network);
        return updatedNetwork;
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) {
        openstackClient.init(vimInstance);
        return openstackClient.listNetworks();
    }

    @Override
    public NFVImage update() {
        //NFVImage updatedImage = openstackClient.updateImage(image);
        return null;
    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimException {
        VimInstance vimInstance = vdu.getVimInstance();
        log.trace("Initializing " + vimInstance);
        openstackClient.init(vimInstance);
        log.debug("initialized VimInstance");
        /**
         *  *) Create hostname
         *  *) choose image
         *  *) ...?
         */
        vdu.setHostname(virtualNetworkFunctionRecord.getName() + "-" + vdu.getId().substring((vdu.getId().length() - 5), vdu.getId().length() - 1));

        String image = this.chooseImage(vdu.getVm_image(), vimInstance);

        List<String> networks = new ArrayList<String>(){{add("7e62608d-efaa-4891-8ea2-3d3f537559c1");}};
        log.trace(""+virtualNetworkFunctionRecord);
        log.trace("");
        String flavorExtId = getFlavorExtID(virtualNetworkFunctionRecord.getDeployment_flavour_key(), vimInstance);
        log.trace("Params: " + vdu.getHostname() + " - " + image + " - " + flavorExtId + " - " + vimInstance.getKeyPair() + " - " + networks + " - " + vimInstance.getSecurityGroups());
        Server server = openstackClient.launchInstanceAndWait(vdu.getHostname(), image, flavorExtId, vimInstance.getKeyPair(), networks, vimInstance.getSecurityGroups(), "#userdata");
        log.debug("launched instance with id " + server.getExtId());
        vdu.setExtId(server.getExtId());
        return new AsyncResult<>(server.getExtId());
    }

    private String getFlavorExtID(String key, VimInstance vimInstance) throws VimException {
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

    @Override
    public Network query(String id) {
        return null;
    }

//     TODO choose the right image (DONE)
    private String chooseImage(List<String> vm_images, VimInstance vimInstance) throws VimException {
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

    @Override
    public void copy() {

    }

    @Override
    public DeploymentFlavour add(DeploymentFlavour deploymentFlavour) {
        DeploymentFlavour flavor = openstackClient.addFlavor(deploymentFlavour);
        return flavor;
    }

    @Override
    public DeploymentFlavour update(DeploymentFlavour new_deploymentFlavour) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance){
        openstackClient.init(vimInstance);
        return openstackClient.listFlavors();
    }
}
