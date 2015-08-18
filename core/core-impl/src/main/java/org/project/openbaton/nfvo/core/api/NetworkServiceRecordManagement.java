/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.nfvo.core.api;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.common.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.nfvo.common.exceptions.*;
import org.project.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.project.openbaton.nfvo.core.interfaces.NetworkManagement;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.project.openbaton.nfvo.core.utils.NSDUtils;
import org.project.openbaton.nfvo.core.utils.NSRUtils;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private EventDispatcher publisher;

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
    private ConfigurationManagement configurationManagement;

    @Autowired
    private NSDUtils nsdUtils;

    @Autowired
    private VnfmManager vnfmManager;

    @Autowired
    private ResourceManagement resourceManagement;

    @Autowired
    private NetworkManagement networkManagement;

    @Autowired
    private VNFLifecycleOperationGranting vnfLifecycleOperationGranting;

    // TODO fetch the NetworkServiceDescriptor from the DB (DONE)
    @Override
    public NetworkServiceRecord onboard(String nsd_id) throws InterruptedException, ExecutionException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.find(nsd_id);
        return deployNSR(networkServiceDescriptor);
    }

    // TODO Removed propagation because I don't remeber why I put it, if it works should be completely removed
    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) throws ExecutionException, InterruptedException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {

        /*
        Create NSR
         */
        nsdUtils.fetchVimInstances(networkServiceDescriptor);
        return deployNSR(networkServiceDescriptor);
    }

    private NetworkServiceRecord deployNSR(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, VimException, InterruptedException, ExecutionException, VimDriverException, QuotaExceededException {
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
        List<String> ids = new ArrayList<>();
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
            if (!events.contains(Event.ALLOCATE)) {
                if (vnfLifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord) == false)
                    throw new QuotaExceededException("Quota exceeded on the deployment of " + virtualNetworkFunctionRecord.getName());
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                    ids.add(resourceManagement.allocate(virtualDeploymentUnit, virtualNetworkFunctionRecord));
                }
            }
        }

        for(String id : ids){
            log.debug("Created VDU with id: " + id);
        }

        /**
         * now check for the requires pointing to the nfvo
         */

        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()){
            for (ConfigurationParameter configurationParameter : virtualNetworkFunctionRecord.getRequires().getConfigurationParameters()){
                log.debug("Checking parameter: " + configurationParameter.getConfKey());
                if (configurationParameter.getConfKey().startsWith("nfvo:")){ //the parameters known from the nfvo
                    StringTokenizer st = new StringTokenizer(configurationParameter.getConfKey(), ":");
                    st.nextToken();
                    for (ConfigurationParameter configurationParameterSystem : configurationManagement.queryByName("system").getConfigurationParameters()){
                        if (configurationParameterSystem.getConfKey().equals(st.nextToken())){
                            log.debug("Found parameter: " + configurationParameterSystem);
                            configurationParameter.setValue(configurationParameterSystem.getValue());
                        }
                    }
                }
            }
        }

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
    public void delete(String id) throws VimException, NotFoundException, InterruptedException, ExecutionException, WrongStatusException {
        NetworkServiceRecord networkServiceRecord = nsrRepository.find(id);

        Configuration configuration = configurationManagement.queryByName("system");

        boolean checkStatus = true;

        for (ConfigurationParameter configurationParameter : configuration.getConfigurationParameters()){
            if (configurationParameter.getConfKey().equals("delete-on-all-status")){
                if (configurationParameter.getValue().equalsIgnoreCase("true")){
                    checkStatus = false;
                    break;
                }
            }
        }

        if (checkStatus){
            if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal() && networkServiceRecord.getStatus().ordinal() != Status.ERROR.ordinal())
                throw new WrongStatusException("The NetworkService " + networkServiceRecord.getId() + " is in the wrong state. ( Status= " + networkServiceRecord.getStatus() +" )");
        }

        List<Future<Void>> futures = new ArrayList<>();
        boolean release = false;
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {
            Set<Event> events = new HashSet<>();
            for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()){
                events.add(lifecycleEvent.getEvent());
                log.debug("found " + lifecycleEvent.getEvent());
            }
            if (!events.contains(Event.RELEASE)) {
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                    futures.add(resourceManagement.release(virtualDeploymentUnit));
                }
                virtualNetworkFunctionRecord.setStatus(Status.TERMINATED);
            }
            else {
                release = true;
                vnfmManager.release(virtualNetworkFunctionRecord);
            }
        }

        for(Future<Void> result : futures){
            result.get();
            log.debug("Deleted VDU");
        }

        /**
         * I think that the NSR should be removed from the NFVO anyway from the catalogue.
         * The VNFM is in charge of releasing resources. The NFVO will receive a notification whether this operation
         * went well or not. But still the NSR should be removed from the DB.
         */
        if (!release) {
            ApplicationEventNFVO event = new ApplicationEventNFVO(this, Action.RELEASE_RESOURCES_FINISH, networkServiceRecord);
            log.debug("Publishing event: " + event);
            publisher.dispatchEvent(event);
            nsrRepository.remove(networkServiceRecord);
        }
    }
}
