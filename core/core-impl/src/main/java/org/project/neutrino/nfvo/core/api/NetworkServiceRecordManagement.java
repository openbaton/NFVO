package org.project.neutrino.nfvo.core.api;

import org.project.neutrino.nfvo.catalogue.mano.common.VNFRecordDependency;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.common.exceptions.BadFormatException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope("prototype")
public class NetworkServiceRecordManagement implements org.project.neutrino.nfvo.core.interfaces.NetworkServiceRecordManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("NSRRepository")
    private GenericRepository<NetworkServiceRecord> nsrRepository;

    @Autowired
    @Qualifier("NSDRepository")
    private GenericRepository<NetworkServiceDescriptor> nsdRepository;

    @Autowired
    @Qualifier("VNFRRepository")
    private GenericRepository<VirtualNetworkFunctionRecord> vnfrRepository;

    @Autowired
    @Qualifier("VNFRDependencyRepository")
    private GenericRepository<VNFRecordDependency> vnfrDependencyRepository;

    @Autowired
    private VimBroker<ResourceManagement> vimBroker;

    @Autowired
    private NSDUtils nsdUtils;

    @Autowired
    private VnfmManager vnfmManager;

    // TODO fetch the NetworkServiceDescriptor from the DB

    @Override
    public NetworkServiceRecord onboard(String nsd_id) throws InterruptedException, ExecutionException, NamingException, VimException, JMSException, NotFoundException, BadFormatException {
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.find(nsd_id);
        return deployNSR(networkServiceDescriptor);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) throws ExecutionException, InterruptedException, VimException, NotFoundException, JMSException, NamingException, BadFormatException {

        /*
        Create NSR
         */
        nsdUtils.fetchVimInstances(networkServiceDescriptor);
        return deployNSR(networkServiceDescriptor);
    }

    private NetworkServiceRecord deployNSR(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, VimException, InterruptedException, ExecutionException, NamingException, JMSException {
        log.debug("Fetched NetworkServiceDescriptor: " + networkServiceDescriptor);
        NetworkServiceRecord networkServiceRecord = NSRUtils.createNetworkServiceRecord(networkServiceDescriptor);

        log.trace("Creating " + networkServiceRecord);

        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr())
            vnfrRepository.create(vnfr);

        log.trace("Persisting VNFDependencies");
        for (VNFRecordDependency vnfrDependency : networkServiceRecord.getVnf_dependency()){
            log.trace("" + vnfrDependency.getSource());
            vnfrDependencyRepository.create(vnfrDependency);
        }
        log.trace("Persisted VNFDependencies");


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
    public NetworkServiceRecord update(NetworkServiceRecord new_nsr, String old_id) {
        NetworkServiceRecord old_nsr = nsrRepository.find(old_id);
        old_nsr.setName(new_nsr.getName());
        old_nsr.setVendor(new_nsr.getVendor());
        old_nsr.setVersion(new_nsr.getVersion());
        return old_nsr;
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
    public void delete(String id) throws VimException {
        NetworkServiceRecord networkServiceRecord = nsrRepository.find(id);
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {
            for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                ResourceManagement vim = vimBroker.getVim(virtualDeploymentUnit.getVimInstance().getType());
                vim.release(virtualDeploymentUnit);
            }
        }
        nsrRepository.remove(networkServiceRecord);
    }
}
