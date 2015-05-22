package org.project.neutrino.nfvo.vim;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
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

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class OpenstackVIM implements ImageManagement, ResourceManagement, NetworkManagement {// TODO and so on...

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("openstackClient")
    private ClientInterfaces openstackClient;

    @Override
    public NFVImage add(NFVImage image) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Network add(Network network) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Network update(Network new_network, String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) {
        openstackClient.init(vimInstance);
        return openstackClient.listNetworks();
    }

    @Override
    public NFVImage update() {
        throw new UnsupportedOperationException();
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
        vdu.setHostname(virtualNetworkFunctionRecord.getName() + "-" + vdu.getId().substring((vdu.getId().length()-4), vdu.getId().length()-1));

        String image = this.chooseImage(vdu.getVm_image(), vimInstance);

        String id = openstackClient.launch_instance(vdu.getHostname(), image, virtualNetworkFunctionRecord.getDeployment_flavour().getFlavour_key(), vimInstance.getKeyPair(), null , vimInstance.getSecurityGroups(), "#userdata");
        log.debug("launched instance with id " + id);
        return new AsyncResult<>(id);
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

    // TODO choose the right image
    private String chooseImage(List<String> vm_images, VimInstance vimInstance) throws VimException {
        if (vm_images != null && vm_images.size() > 0)
            return vm_images.get(0);
        else
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
}
