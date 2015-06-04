package org.project.neutrino.nfvo.vim;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.catalogue.util.IdGenerator;
import org.project.neutrino.nfvo.vim_interfaces.DeploymentFlavorManagement;
import org.project.neutrino.nfvo.vim_interfaces.ImageManagement;
import org.project.neutrino.nfvo.vim_interfaces.NetworkManagement;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
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
public class TestVIM implements ImageManagement, ResourceManagement, NetworkManagement, DeploymentFlavorManagement{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("testClient")
    private ClientInterfaces testClient;

    @Override
    public NFVImage add(NFVImage image) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Network add(Network network) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeploymentFlavour add(DeploymentFlavour deploymentFlavour) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public DeploymentFlavour update(DeploymentFlavour new_deploymentFlavour) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) {
        return testClient.listFlavors();
    }

    @Override
    public Network update(Network new_network, String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) {
        return testClient.listNetworks();
    }

    @Override
    public Network query(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NFVImage update() {throw new UnsupportedOperationException();

    }

    @Override
    public List<NFVImage> queryImages(VimInstance vimInstance) {
        return testClient.listImages();
    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        VimInstance vimInstance = vdu.getVimInstance();
        log.trace("Initializing " + vimInstance);
        testClient.launchInstanceAndWait(vdu.getHostname(),vimInstance.getImages().get(0).getExtId(),"flavor","keypair",new ArrayList<String>(){{add("network_id");}}, new ArrayList<String>(){{add("secGroup_id");}}, "#userdate");
        String id = IdGenerator.createUUID();
        log.debug("launched instance with id " + id);
        return new AsyncResult<String>(id);
    }

    @Override
    public List<Server> queryResources(VimInstance vimInstance) {

        return testClient.listServer();
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
