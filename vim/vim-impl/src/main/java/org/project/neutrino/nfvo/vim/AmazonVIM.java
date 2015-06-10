package org.project.neutrino.nfvo.vim;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.ImageManagement;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
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
public class AmazonVIM implements ImageManagement, ResourceManagement {

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
}
