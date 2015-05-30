package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.core.utils.NSDUtils;
import org.project.neutrino.nfvo.core.utils.NSRUtils;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.project.neutrino.nfvo.vim_interfaces.ResourceManagement;
import org.project.neutrino.nfvo.vim_interfaces.VimBroker;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.project.neutrino.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope
public class NetworkServiceRecordManagement implements org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("NSRRepository")
    private GenericRepository<NetworkServiceRecord> nsrRepository;


    @Autowired
    private VimBroker<ResourceManagement> vimBroker;

    @Autowired
    private NSDUtils nsdUtils;

    @Autowired
    private VnfmManager vnfmManager;

    @Override
    public NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) throws ExecutionException, InterruptedException, VimException, NotFoundException {

        /*
        Create NSR
         */
        nsdUtils.fetchData(networkServiceDescriptor);
        NetworkServiceRecord networkServiceRecord = NSRUtils.createNetworkServiceRecord(networkServiceDescriptor);
        log.trace("Deploying " + networkServiceRecord);
        nsrRepository.create(networkServiceRecord);


        log.debug("created NetworkServiceRecord with id " + networkServiceRecord.getId());

        /*
         * Getting the vim based on the VDU datacenter type
         * Calling the vim to create the Resources
         */
        List<Future<String>> ids = new ArrayList<>();
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()){
            for(VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                ResourceManagement vim = vimBroker.getVim(virtualDeploymentUnit.getVimInstance().getType());
                ids.add(vim.allocate(virtualDeploymentUnit,virtualNetworkFunctionRecord));
            }
        }

        for(Future<String> id : ids){
            log.debug("Created VDU with id: " + id.get());
        }

        /**
         * TODO start the VNF installation process:
         *  *) call the VNFMRegister
         *      *) the Register knows that all the VNFMs are available
         *      *) the Register knows which protocol to use per VNFM
         *
         *  for instance...
         */

        vnfmManager.deploy(networkServiceRecord);

        return networkServiceRecord;
    }

    @Override
    public NetworkServiceRecord update(NetworkServiceRecord new_nsd, String old_id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NetworkServiceRecord> query() {
        return nsrRepository.findAll();
    }

    @Override
    public NetworkServiceRecord query(String id) {
        return nsrRepository.find(id);
    }

    @Override
    public void delete(String id) {
        nsrRepository.remove(nsrRepository.find(id));
    }
}
