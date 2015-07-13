package org.project.openbaton.nfvo.core.api;

import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.common.catalogue.mano.common.Event;
import org.project.openbaton.common.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.common.catalogue.mano.common.VNFRecordDependency;
import org.project.openbaton.common.catalogue.mano.descriptor.InternalVirtualLink;
import org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.common.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.common.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.common.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.common.catalogue.nfvo.Network;
import org.project.openbaton.common.catalogue.nfvo.Subnet;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.openbaton.nfvo.core.utils.NSRUtils;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.project.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.project.openbaton.nfvo.core.interfaces.NetworkManagement;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope("prototype")
public class NetworkServiceRecordManagement implements org.project.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement {

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
    private NSDUtils nsdUtils;

    @Autowired
    private VnfmManager vnfmManager;

    @Autowired
    private ResourceManagement resourceManagement;

    @Autowired
    private NetworkManagement networkManagement;

    // TODO fetch the NetworkServiceDescriptor from the DB

    @Override
    public NetworkServiceRecord onboard(String nsd_id) throws InterruptedException, ExecutionException, NamingException, VimException, JMSException, NotFoundException, BadFormatException, VimDriverException {
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.find(nsd_id);
        return deployNSR(networkServiceDescriptor);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) throws ExecutionException, InterruptedException, VimException, NotFoundException, JMSException, NamingException, BadFormatException, VimDriverException {

        /*
        Create NSR
         */
        nsdUtils.fetchVimInstances(networkServiceDescriptor);
        return deployNSR(networkServiceDescriptor);
    }

    private NetworkServiceRecord deployNSR(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, VimException, InterruptedException, ExecutionException, NamingException, JMSException, VimDriverException {
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
            for (InternalVirtualLink internalVirtualLink : virtualNetworkFunctionRecord.getVirtual_link()) {
                if (internalVirtualLink.getConnectivity_type().equals("LAN")) {
                    for (String connectionPointReference : internalVirtualLink.getConnection_points_references()) {
                        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
                            boolean networkExists = false;
                            for (Network network : vdu.getVimInstance().getNetworks()) {
                                if (network.getName().equals(connectionPointReference) || network.getExtId().equals(connectionPointReference)) {
                                    networkExists = true;
                                    NSRUtils.createConnectionsPoints(virtualNetworkFunctionRecord, vdu, network);
                                    break;
                                }
                            }
                            if (networkExists == false) {
                                Network network = new Network();
                                network.setName(connectionPointReference);
                                network.setSubnets(new HashSet<Subnet>());
                                network = networkManagement.add(vdu.getVimInstance(), network);
                                NSRUtils.createConnectionsPoints(virtualNetworkFunctionRecord, vdu, network);
                            }
                        }
                    }
                }
            }
            Set<Event> events = new HashSet<>();
            for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()){
                events.add(lifecycleEvent.getEvent());
            }
            if (!events.contains(Event.ALLOCATE))
                for(VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                    ids.add(resourceManagement.allocate(virtualDeploymentUnit,virtualNetworkFunctionRecord));
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
    public void delete(String id) throws VimException, NotFoundException, NamingException, JMSException, InterruptedException, ExecutionException {
        NetworkServiceRecord networkServiceRecord = nsrRepository.find(id);
        List<Future<Void>> futures = new ArrayList<>();
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {
            Set<Event> events = new HashSet<>();
            for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()){
                events.add(lifecycleEvent.getEvent());
                log.debug("found " + lifecycleEvent.getEvent());
            }
            if (!events.contains(Event.RELEASE))
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                    resourceManagement.release(virtualDeploymentUnit);
                }
            else {
                futures.add(vnfmManager.release(virtualNetworkFunctionRecord));
            }
        }

        for(Future<Void> result : futures){
            result.get();
            log.debug("Deleted VDU");
        }

        nsrRepository.remove(networkServiceRecord);
    }
}
