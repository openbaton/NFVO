package org.project.openbaton.nfvo.vim;

import org.project.openbaton.common.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.common.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.common.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.common.catalogue.nfvo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class AmazonVIM implements Vim {

    @Autowired
    @Qualifier("amazonClient")
    private ClientInterfaces amazonClient;

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
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(VimInstance vimInstance, NFVImage image, InputStream inputStream) throws VimException {

    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Server> queryResources(VimInstance vimInstance) {
        throw new UnsupportedOperationException();
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
    public Quota getQuota(VimInstance vimInstance) {
        return null;
    }

    @Override
    public DeploymentFlavour add(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        return null;
    }

    @Override
    public void delete(VimInstance vimInstance, DeploymentFlavour deploymentFlavor) throws VimException {

    }

    @Override
    public DeploymentFlavour update(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimException {
        return null;
    }

    @Override
    public List<DeploymentFlavour> queryDeploymentFlavors(VimInstance vimInstance) throws VimException {
        return null;
    }

    @Override
    public Network add(VimInstance vimInstance, Network network) throws VimException {
        return null;
    }

    @Override
    public void delete(VimInstance vimInstance, Network network) throws VimException {

    }

    @Override
    public Network update(VimInstance vimInstance, Network updatingNetwork) throws VimException {
        return null;
    }

    @Override
    public List<Network> queryNetwork(VimInstance vimInstance) throws VimException {
        return null;
    }

    @Override
    public Network query(VimInstance vimInstance, String extId) throws VimException {
        return null;
    }
}
