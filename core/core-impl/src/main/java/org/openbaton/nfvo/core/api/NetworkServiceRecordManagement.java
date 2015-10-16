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

package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.record.*;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.openbaton.nfvo.core.interfaces.NetworkManagement;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.core.utils.NSRUtils;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.VNFCRepository;
import org.openbaton.nfvo.repositories.VNFRRepository;
import org.openbaton.vim.drivers.exceptions.VimDriverException;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
public class NetworkServiceRecordManagement implements org.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EventDispatcher publisher;

    @Autowired
    private NetworkServiceRecordRepository nsrRepository;

    @Autowired
    private NetworkServiceDescriptorRepository nsdRepository;

    @Autowired
    private VNFRRepository vnfrRepository;

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
    private DependencyManagement dependencyManagement;

    @Autowired
    private VNFCRepository vnfcRepository;

    @Override
    public NetworkServiceRecord onboard(String idNsd) throws InterruptedException, ExecutionException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
        log.debug("Looking for NetworkServiceDescriptor with id: " + idNsd);
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(idNsd);
        return deployNSR(networkServiceDescriptor);
    }

    @Override
    public NetworkServiceRecord onboard(NetworkServiceDescriptor networkServiceDescriptor) throws ExecutionException, InterruptedException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
        nsdUtils.fetchVimInstances(networkServiceDescriptor);
        return deployNSR(networkServiceDescriptor);
    }

    public void deleteVNFRecord(String idNsr, String idVnf) {
        //TODO the logic of this request for the moment deletes only the VNFR from the DB, need to be removed from the running NetworkServiceRecord
        nsrRepository.deleteVNFRecord(idNsr, idVnf);
    }

    /**
     * Returns the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
     *
     * @param idNsr of Nsr
     * @param idVnf of VirtualNetworkFunctionRecord
     * @return VirtualNetworkFunctionRecord selected
     */
    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String idNsr, String idVnf) {
        nsrRepository.exists(idNsr);
        return vnfrRepository.findFirstById(idVnf);
    }

    /**
     * Deletes the VNFDependency with idVnfr into NSR with idNsr
     *
     * @param idNsr  of NSR
     * @param idVnfd of VNFDependency
     */
    @Override
    public void deleteVNFDependency(String idNsr, String idVnfd) {
        //TODO the logic of this request for the moment deletes only the VNFR from the DB, need to be removed from the running NetworkServiceRecord
        nsrRepository.deleteVNFDependency(idNsr, idVnfd);
    }

    @Override
    public void addVNFCInstance(String id, String idVnf, String idVdu, VNFComponent component) throws NotFoundException, BadFormatException, WrongStatusException {
        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(id);
        if (networkServiceRecord == null)
            throw new NotFoundException("No NetworkServiceRecord found with id " + id);

        if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal()){
            throw new WrongStatusException("NetworkServiceDescriptor must be in ACTIVE state");
        }
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = null;
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord1 : networkServiceRecord.getVnfr()){
            if (virtualNetworkFunctionRecord1.getId().equals(idVnf)){
                virtualNetworkFunctionRecord = virtualNetworkFunctionRecord1;
                break;
            }
        }
        if (virtualNetworkFunctionRecord == null)
            throw new NotFoundException("No VirtualNetworkFunctionRecord found with id " + idVnf);

        VirtualDeploymentUnit virtualDeploymentUnit = null;
        for (VirtualDeploymentUnit virtualDeploymentUnit1 : virtualNetworkFunctionRecord.getVdu()){
            if (virtualDeploymentUnit1.getId().equals(idVdu)){
                virtualDeploymentUnit = virtualDeploymentUnit1;
            }
        }
        if (virtualDeploymentUnit == null)
            throw new NotFoundException("No VirtualDeploymentUnit found with id " + idVdu);

        List<String> componentNetworks = new ArrayList<>();

        for (VNFDConnectionPoint connectionPoint : component.getConnection_point()){
            componentNetworks.add(connectionPoint.getVirtual_link_reference());
        }

        List<String> vnfrNetworks = new ArrayList<>();

        for (InternalVirtualLink virtualLink : virtualNetworkFunctionRecord.getVirtual_link()){
            vnfrNetworks.add(virtualLink.getName());
        }

        if (!vnfrNetworks.containsAll(componentNetworks)){
            throw new BadFormatException("Not all the network exist in the InternalVirtualLinks. They need to be included in these names: " + vnfrNetworks);
        }

        log.info("Adding VNFComponent to VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName());
        virtualDeploymentUnit.getVnfc().add(component);
        vnfcRepository.save(component);
        nsrRepository.save(networkServiceRecord);
        log.debug("new VNFComponent is " + component);

        VNFRecordDependency dependencyTarget = dependencyManagement.getDependencyForAVNFRecordTarget(virtualNetworkFunctionRecord);

        log.debug("Found Dependency: " + dependencyTarget);

        vnfmManager.addVnfc(virtualNetworkFunctionRecord,virtualDeploymentUnit,component, dependencyTarget);
    }

    private NetworkServiceRecord deployNSR(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, VimException, InterruptedException, ExecutionException, VimDriverException, QuotaExceededException {
        log.trace("Fetched NetworkServiceDescriptor: " + networkServiceDescriptor);
        NetworkServiceRecord networkServiceRecord;
        networkServiceRecord = NSRUtils.createNetworkServiceRecord(networkServiceDescriptor);
        log.trace("Creating " + networkServiceRecord);

        /*
         * Getting the vim based on the VDU datacenter type
         */
        for (VirtualLinkRecord vlr : networkServiceRecord.getVlr()) {
            for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
                for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
                    for (VNFComponent vnfc : vdu.getVnfc()) {
                        for (VNFDConnectionPoint vnfdConnectionPoint : vnfc.getConnection_point()) {
                            if (vnfdConnectionPoint.getVirtual_link_reference().equals(vlr.getName())) {
                                boolean networkExists = false;
                                for (Network network : vdu.getVimInstance().getNetworks()) {
                                    if (network.getName().equals(vlr.getName()) || network.getExtId().equals(vlr.getName())) {
                                        networkExists = true;
                                        vlr.setStatus(LinkStatus.NORMALOPERATION);
                                        vlr.setVim_id(vdu.getId());
                                        vlr.setExtId(network.getExtId());
                                        vlr.getConnection().add(vnfdConnectionPoint.getId());
                                        break;
                                    }
                                }
                                if (networkExists == false) {
                                    Network network = new Network();
                                    network.setName(vlr.getName());
                                    network.setSubnets(new HashSet<Subnet>());
                                    network = networkManagement.add(vdu.getVimInstance(), network);
                                    vlr.setStatus(LinkStatus.NORMALOPERATION);
                                    vlr.setVim_id(vdu.getId());
                                    vlr.setExtId(network.getExtId());
                                    vlr.getConnection().add(vnfdConnectionPoint.getId());
                                }
                            }
                        }
                    }
                }
            }

//            Set<Event> events = new HashSet<Event>();
//            for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionDescriptor.getLifecycle_event()) {
//                events.add(lifecycleEvent.getEvent());
//            }
//
//            /*if (!events.contains(Event.ALLOCATE)) {
//                if (vnfLifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionDescriptor) == false)
//                    throw new QuotaExceededException("Quota exceeded on the deployment of " + virtualNetworkFunctionDescriptor.getName());
//                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionDescriptor.getVdu()) {
//                    ids.add(resourceManagement.allocate(virtualDeploymentUnit, virtualNetworkFunctionDescriptor));
//                }
//            }*/
        }

//        for (String id : ids) {
//            log.debug("Created VDU with id: " + id);
//        }

        NSRUtils.setDependencies(networkServiceDescriptor, networkServiceRecord);

        networkServiceRecord = nsrRepository.save(networkServiceRecord);

        vnfmManager.deploy(networkServiceDescriptor, networkServiceRecord);

        /*log.debug("VNFR are: ");
        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()){
            log.debug(vnfr.getName());
        }*/
        /**
         * now check for the requires pointing to the nfvo
         */
        //TODO check where to put this
        /*for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()){
            for (ConfigurationParameter configurationParameter : virtualNetworkFunctionRecord.getRequires().getConfigurationParameters()){
                log.debug("Checking parameter: " + configurationParameter.getConfKey());
                if (configurationParameter.getConfKey().startsWith("nfvo:")){ //the parameters known from the nfvo
                    String[] params = configurationParameter.getConfKey().split("\\:");
                    for (ConfigurationParameter configurationParameterSystem : configurationManagement.queryByName("system").getConfigurationParameters()){
                        if (configurationParameterSystem.getConfKey().equals(params[1])){
                            log.debug("Found parameter: " + configurationParameterSystem);
                            configurationParameter.setValue(configurationParameterSystem.getValue());
                        }
                    }
                }
            }
        }*/


        return networkServiceRecord;
    }

    @Override
    public NetworkServiceRecord update(NetworkServiceRecord newRsr, String idNsr) {
        nsrRepository.exists(idNsr);
        newRsr = nsrRepository.save(newRsr);
        return newRsr;
    }

    @Override
    public Iterable<NetworkServiceRecord> query() {
        return nsrRepository.findAll();
    }

    @Override
    public NetworkServiceRecord query(String id) {
        return nsrRepository.findFirstById(id);
    }

    @Override
    public void delete(String id) throws VimException, NotFoundException, InterruptedException, ExecutionException, WrongStatusException {
        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(id);
        Configuration configuration = configurationManagement.queryByName("system");

        boolean checkStatus = true;

        for (ConfigurationParameter configurationParameter : configuration.getConfigurationParameters()) {
            if (configurationParameter.getConfKey().equals("delete-on-all-status")) {
                if (configurationParameter.getValue().equalsIgnoreCase("true")) {
                    checkStatus = false;
                    break;
                }
            }
        }

        if (networkServiceRecord.getStatus().ordinal() == Status.NULL.ordinal())
            throw new WrongStatusException("The NetworkService " + networkServiceRecord.getId() + " is in the wrong state. ( Status= " + networkServiceRecord.getStatus() + " )");

        if (checkStatus) {
            if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal() && networkServiceRecord.getStatus().ordinal() != Status.ERROR.ordinal())
                throw new WrongStatusException("The NetworkService " + networkServiceRecord.getId() + " is in the wrong state. ( Status= " + networkServiceRecord.getStatus() + " )");
        }

        List<Future<Void>> futures = new ArrayList<Future<Void>>();
        boolean release = false;
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {
            Set<Event> events = new HashSet<Event>();
            for (LifecycleEvent lifecycleEvent : virtualNetworkFunctionRecord.getLifecycle_event()) {
                events.add(lifecycleEvent.getEvent());
                log.debug("found " + lifecycleEvent.getEvent());
            }
            if (!events.contains(Event.RELEASE)) {
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                        futures.add(resourceManagement.release(virtualDeploymentUnit, vnfcInstance));
                    }
                }
                virtualNetworkFunctionRecord.setStatus(Status.TERMINATED);
            } else {
                release = true;
                vnfmManager.release(virtualNetworkFunctionRecord);
            }
        }

        for (Future<Void> result : futures) {
            result.get();
            log.debug("Deleted VDU");
        }

        if (!release) {
            ApplicationEventNFVO event = new ApplicationEventNFVO(Action.RELEASE_RESOURCES_FINISH, networkServiceRecord);
            EventNFVO eventNFVO = new EventNFVO(this);
            eventNFVO.setEventNFVO(event);
            log.debug("Publishing event: " + event);
            publisher.dispatchEvent(eventNFVO);
            nsrRepository.delete(networkServiceRecord);
        }
    }
}
