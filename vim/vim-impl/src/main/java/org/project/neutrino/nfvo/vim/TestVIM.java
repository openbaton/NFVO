package org.project.neutrino.nfvo.vim;

import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.*;
import org.project.neutrino.nfvo.catalogue.util.IdGenerator;
import org.project.neutrino.nfvo.common.exceptions.VimException;
import org.project.neutrino.nfvo.vim_interfaces.vim.Vim;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
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
public class TestVIM implements Vim {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("testClient")
    private ClientInterfaces testClient;


    @Override
    public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException {

    }

    @Override
    public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) {
        return testClient.listFlavors();
    }


    @Override
    public NFVImage add(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(VimInstance vimInstance, NFVImage image) throws VimException {

    }

    @Override
    public NFVImage update(VimInstance vimInstance, NFVImage image) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NFVImage> queryImages(VimInstance vimInstance) {
        return testClient.listImages();
    }

    @Override
    public void copy(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {

    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimException {
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
    public Network add(VimInstance vimInstance, Network network) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException {

    }

    @Override
    public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
        return this.testClient.listNetworks();
    }

    @Override
    public Network query(VimInstance vimInstance, String extId) throws VimException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Quota getQuota(VimInstance vimInstance) {
        return null;
    }
}
