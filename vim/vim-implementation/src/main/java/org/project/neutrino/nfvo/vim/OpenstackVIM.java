package org.project.neutrino.nfvo.vim;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.client_interfaces.ClientInterfaces;
import org.project.neutrino.nfvo.vim_interfaces.ImageManagement;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * Created by lto on 12/05/15.
 */
@Service
@Scope("prototype")
public class OpenstackVIM implements ImageManagement, ResourceManagement {// TODO and so on...

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("OpenstackClient")
    private ClientInterfaces openstackClient;

    @Override
    public void add() {
    }

    @Override
    public void delete() {

    }

    @Override
    public void update() {

    }

    @Override
    @Async
    public Future<String> allocate(VirtualDeploymentUnit vdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
        VimInstance vimInstance = vdu.getVimInstance();
        log.trace("Initializing " + vimInstance);
        openstackClient.init(vimInstance);
        log.debug("initialized VimInstance");
        String id = openstackClient.launch_instance(vdu.getHostname(), vdu.getVm_image().get(0), virtualNetworkFunctionRecord.getDeployment_flavour().getFlavour_key(), vimInstance.getKeyPair(), null , vimInstance.getSecurityGroups(), "#userdata");
        log.debug("launched instance with id " + id);
        return new AsyncResult<String>(id);
    }

    @Override
    public void query() {

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
