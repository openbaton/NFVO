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

import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.mano.record.*;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmHealVNFRequestMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.common.internal.model.EventNFVO;
import org.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.openbaton.nfvo.core.interfaces.EventDispatcher;
import org.openbaton.nfvo.core.interfaces.NetworkManagement;
import org.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.openbaton.nfvo.core.utils.NSDUtils;
import org.openbaton.nfvo.core.utils.NSRUtils;
import org.openbaton.nfvo.repositories.*;
import org.openbaton.vnfm.interfaces.manager.VnfmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by lto on 11/05/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties
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
    private VNFRecordDependencyRepository vnfRecordDependencyRepository;

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

    @Autowired
    private VduRepository vduRepository;

    @Value("${nfvo.delete.all-status:}")
    private String deleteInAllStatus;

    @Value("${nfvo.delete.vnfr:false}")
    private String waitForDelete;

    @Autowired
    private VimRepository vimInstanceRepository;
    @Value("${nfvo.delete.vnfr:false}")
    private boolean removeAfterTimeout;
    private ThreadPoolTaskExecutor asyncExecutor;

    @Value("${nfvo.delete.vnfr.wait:60}")
    private int timeout;


    @PostConstruct
    private void init() {
        if (removeAfterTimeout) {
            asyncExecutor = new ThreadPoolTaskExecutor();
            asyncExecutor.setThreadNamePrefix("OpenbatonTask-");
            asyncExecutor.setMaxPoolSize(30);
            asyncExecutor.setCorePoolSize(5);
            asyncExecutor.setQueueCapacity(0);
            asyncExecutor.setKeepAliveSeconds(20);
            asyncExecutor.initialize();
        }
    }

    @Override
    public NetworkServiceRecord onboard(String idNsd) throws InterruptedException, ExecutionException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
        log.info("Looking for NetworkServiceDescriptor with id: " + idNsd);
        NetworkServiceDescriptor networkServiceDescriptor = nsdRepository.findFirstById(idNsd);
        if (networkServiceDescriptor == null) {
            throw new NotFoundException("NSD with id " + idNsd + " was not found");
        }
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
    public void addVNFCInstance(String id, String idVnf, VNFComponent component) throws NotFoundException, BadFormatException, WrongStatusException {
        log.info("Adding new VNFCInstance to VNFR with id: " + idVnf);
        NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);

        VirtualDeploymentUnit virtualDeploymentUnit = virtualNetworkFunctionRecord.getVdu().iterator().next();
        if (virtualDeploymentUnit == null) {
            throw new NotFoundException("No VirtualDeploymentUnit found");
        }

        if (virtualDeploymentUnit.getScale_in_out() == virtualDeploymentUnit.getVnfc_instance().size()) {
            throw new WrongStatusException("The VirtualDeploymentUnit chosen has reached the maximum number of VNFCInstance");
        }
        networkServiceRecord.setStatus(Status.SCALING);
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        scaleOUT(networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, component, "");
    }

    @Override
    public void addVNFCInstance(String id, String idVnf, String idVdu, VNFComponent component, String mode) throws NotFoundException, BadFormatException, WrongStatusException {
        log.info("Adding new VNFCInstance to VNFR with id: " + idVnf);
        NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);

        VirtualDeploymentUnit virtualDeploymentUnit = getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);

        if (virtualDeploymentUnit.getScale_in_out() == virtualDeploymentUnit.getVnfc_instance().size()) {
            throw new WrongStatusException("The VirtualDeploymentUnit chosen has reached the maximum number of VNFCInstance");
        }

        networkServiceRecord.setStatus(Status.SCALING);
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        scaleOUT(networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, component, mode);
    }

    private void scaleOUT(NetworkServiceRecord networkServiceRecord, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VirtualDeploymentUnit virtualDeploymentUnit, VNFComponent component, String mode) throws BadFormatException, NotFoundException {
        List<String> componentNetworks = new ArrayList<>();

        for (VNFDConnectionPoint connectionPoint : component.getConnection_point()) {
            componentNetworks.add(connectionPoint.getVirtual_link_reference());
        }

        List<String> vnfrNetworks = new ArrayList<>();

        for (InternalVirtualLink virtualLink : virtualNetworkFunctionRecord.getVirtual_link()) {
            vnfrNetworks.add(virtualLink.getName());
        }

        if (!vnfrNetworks.containsAll(componentNetworks)) {
            throw new BadFormatException("Not all the network exist in the InternalVirtualLinks. They need to be included in these names: " + vnfrNetworks);
        }

        log.info("Adding VNFComponent to VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName());
        virtualDeploymentUnit.getVnfc().add(component);
        vnfcRepository.save(component);
        nsrRepository.save(networkServiceRecord);
        log.debug("new VNFComponent is " + component);

        VNFRecordDependency dependencyTarget = dependencyManagement.getDependencyForAVNFRecordTarget(virtualNetworkFunctionRecord);

        log.debug("Found Dependency: " + dependencyTarget);

        vnfmManager.addVnfc(virtualNetworkFunctionRecord, component, dependencyTarget, mode);
    }

    @Override
    public void deleteVNFCInstance(String id, String idVnf) throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException, VimException {
        log.info("Removing new VNFCInstance from VNFR with id: " + idVnf);
        NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);

        VirtualDeploymentUnit virtualDeploymentUnit = virtualNetworkFunctionRecord.getVdu().iterator().next();
        if (virtualDeploymentUnit == null) {
            throw new NotFoundException("No VirtualDeploymentUnit found");
        }

        if (virtualDeploymentUnit.getVnfc_instance().size() == 1) {
            throw new WrongStatusException("The VirtualDeploymentUnit chosen has reached the minimum number of VNFCInstance");
        }

        VNFCInstance vnfcInstance = virtualDeploymentUnit.getVnfc_instance().iterator().next();
        if (vnfcInstance == null)
            throw new NotFoundException("No VNFCInstance was not found");

        networkServiceRecord.setStatus(Status.SCALING);
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        scaleIn(networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, vnfcInstance);
    }

    @Override
    public void deleteVNFCInstance(String id, String idVnf, String idVdu) throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException, VimException {
        log.info("Removing new VNFCInstance from VNFR with id: " + idVnf + " in vdu: " + idVdu);
        NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);

        VirtualDeploymentUnit virtualDeploymentUnit = null;

        for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
            if (vdu.getId().equals(idVdu)) {
                virtualDeploymentUnit = vdu;
            }
        }

        if (virtualDeploymentUnit == null) {
            throw new NotFoundException("No VirtualDeploymentUnit found");
        }

        if (virtualDeploymentUnit.getVnfc_instance().size() == 1) {
            throw new WrongStatusException("The VirtualDeploymentUnit chosen has reached the minimum number of VNFCInstance");
        }

        VNFCInstance vnfcInstance = virtualDeploymentUnit.getVnfc_instance().iterator().next();
        if (vnfcInstance == null)
            throw new NotFoundException("No VNFCInstance was not found");

        networkServiceRecord.setStatus(Status.SCALING);
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        scaleIn(networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, vnfcInstance);
    }

    @Override
    public void deleteVNFCInstance(String id, String idVnf, String idVdu, String idVNFCI) throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException, VimException {
        NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);

        VirtualDeploymentUnit virtualDeploymentUnit = getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);

        if (virtualDeploymentUnit.getVnfc_instance().size() == 1) {

            throw new WrongStatusException("The VirtualDeploymentUnit chosen has reached the minimum number of VNFCInstance");
        }

        networkServiceRecord.setStatus(Status.SCALING);
        networkServiceRecord = nsrRepository.save(networkServiceRecord);
        scaleIn(networkServiceRecord, virtualNetworkFunctionRecord, virtualDeploymentUnit, getVNFCI(virtualDeploymentUnit, idVNFCI));
    }

    @Override
    public void switchToRedundantVNFCInstance(String id, String idVnf, String idVdu, String idVNFC, String mode, VNFCInstance failedVnfcInstance) throws NotFoundException, WrongStatusException {
        NetworkServiceRecord networkServiceRecord = getNetworkServiceRecordInActiveState(id);
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);

        VirtualDeploymentUnit virtualDeploymentUnit = getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);

        VNFCInstance standByVNFCInstance = null;
        for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
            log.debug("current vnfcinstance " + vnfcInstance + " in state" + vnfcInstance.getState());
            if (vnfcInstance.getState() != null && vnfcInstance.getState().equals(mode)) {
                standByVNFCInstance = vnfcInstance;
                log.debug("VNFComponentInstance in " + mode + " mode FOUND :" + standByVNFCInstance);
            }
            if (vnfcInstance.getId().equals(failedVnfcInstance.getId())) {
                vnfcInstance.setState("failed");
                log.debug("The vnfcInstance: " + vnfcInstance.getHostname() + " is set to '" + vnfcInstance.getState() + "' state");
            }
        }
        if (standByVNFCInstance == null)
            throw new NotFoundException("No VNFCInstance in " + mode + " mode found, so switch to redundant VNFC is not possibile");

        //save the new state of the failedVnfcInstance
        nsrRepository.save(networkServiceRecord);

        OrVnfmHealVNFRequestMessage healMessage = new OrVnfmHealVNFRequestMessage();
        healMessage.setAction(Action.HEAL);
        healMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
        healMessage.setVnfcInstance(standByVNFCInstance);
        healMessage.setCause("switchToStandby");

        vnfmManager.sendMessageToVNFR(virtualNetworkFunctionRecord, healMessage);
    }

    private VNFCInstance getVNFCI(VirtualDeploymentUnit virtualDeploymentUnit, String idVNFCI) throws NotFoundException {
        for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance())
            if (idVNFCI.equals(vnfcInstance.getId()))
                return vnfcInstance;

        throw new NotFoundException("VNFCInstance with id " + idVNFCI + " was not found");
    }

    private void scaleIn(NetworkServiceRecord networkServiceRecord, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, VirtualDeploymentUnit virtualDeploymentUnit, VNFCInstance vnfcInstance) throws NotFoundException, InterruptedException, ExecutionException, VimException {
        List<VNFRecordDependency> dependencySource = dependencyManagement.getDependencyForAVNFRecordSource(virtualNetworkFunctionRecord);

        if (dependencySource.size() != 0) {
            for (VNFRecordDependency dependency : dependencySource) {
                List<String> paramsToRemove = new ArrayList<>();
                for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord1 : networkServiceRecord.getVnfr())
                    if (virtualNetworkFunctionRecord1.getName().equals(dependency.getTarget())) {
                        vnfmManager.removeVnfcDependency(virtualNetworkFunctionRecord1, vnfcInstance);
                        for (Map.Entry<String, VNFCDependencyParameters> parametersEntry : dependency.getVnfcParameters().entrySet()) {
                            log.debug("Parameter: " + parametersEntry);
                            if (parametersEntry.getValue() != null)
                                parametersEntry.getValue().getParameters().remove(vnfcInstance.getId());
                        }

                    }
                for (String paramToRemove : paramsToRemove)
                    dependency.getVnfcParameters().remove(paramToRemove);

                vnfRecordDependencyRepository.save(dependency);
            }
        }

        resourceManagement.release(virtualDeploymentUnit, vnfcInstance);

        virtualDeploymentUnit.getVnfc_instance().remove(vnfcInstance);


        vduRepository.save(virtualDeploymentUnit);

        ApplicationEventNFVO event = new ApplicationEventNFVO(Action.SCALE_OUT, virtualNetworkFunctionRecord);
        EventNFVO eventNFVO = new EventNFVO(this);
        eventNFVO.setEventNFVO(event);
        log.debug("Publishing event: " + event);
        publisher.dispatchEvent(eventNFVO);

        networkServiceRecord.setStatus(Status.ACTIVE);
        nsrRepository.save(networkServiceRecord);
    }

    private VirtualDeploymentUnit getVirtualDeploymentUnit(String idVdu, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException {
        VirtualDeploymentUnit virtualDeploymentUnit = null;
        for (VirtualDeploymentUnit virtualDeploymentUnit1 : virtualNetworkFunctionRecord.getVdu()) {
            if (virtualDeploymentUnit1.getId().equals(idVdu)) {
                virtualDeploymentUnit = virtualDeploymentUnit1;
            }
        }
        if (virtualDeploymentUnit == null)
            throw new NotFoundException("No VirtualDeploymentUnit found with id " + idVdu);
        return virtualDeploymentUnit;
    }

    private VNFCInstance getVNFCInstance(String idVNFCInstance, VirtualDeploymentUnit vdu) throws NotFoundException {
        VNFCInstance vnfcInstance = null;
        for (VNFCInstance currentVnfcInstance : vdu.getVnfc_instance()) {
            if (currentVnfcInstance.getId().equals(idVNFCInstance)) {
                vnfcInstance = currentVnfcInstance;
                break;
            }
        }
        if (vnfcInstance == null)
            throw new NotFoundException("No VnfcInstance found with id " + idVNFCInstance);
        return vnfcInstance;
    }

    private VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String idVnf, NetworkServiceRecord networkServiceRecord) throws NotFoundException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = null;
        for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord1 : networkServiceRecord.getVnfr()) {
            if (virtualNetworkFunctionRecord1.getId().equals(idVnf)) {
                virtualNetworkFunctionRecord = virtualNetworkFunctionRecord1;
                break;
            }
        }
        if (virtualNetworkFunctionRecord == null)
            throw new NotFoundException("No VirtualNetworkFunctionRecord found with id " + idVnf);
        return virtualNetworkFunctionRecord;
    }

    private synchronized NetworkServiceRecord getNetworkServiceRecordInActiveState(String id) throws NotFoundException, WrongStatusException {
        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(id);
        if (networkServiceRecord == null)
            throw new NotFoundException("No NetworkServiceRecord found with id " + id);

        if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal()) {
            throw new WrongStatusException("NetworkServiceDescriptor must be in ACTIVE state");
        }

        return networkServiceRecord;
    }

    private NetworkServiceRecord deployNSR(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException, VimException, InterruptedException, ExecutionException, VimDriverException, QuotaExceededException {
        log.info("Fetched NetworkServiceDescriptor: " + networkServiceDescriptor.getName());
        log.info("VNFD are: ");
        for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd())
            log.debug("\t" + virtualNetworkFunctionDescriptor.getName());

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
                    VimInstance vimInstance = vimInstanceRepository.findFirstByName(vdu.getVimInstanceName());
                    for (VNFComponent vnfc : vdu.getVnfc()) {
                        for (VNFDConnectionPoint vnfdConnectionPoint : vnfc.getConnection_point()) {
                            if (vnfdConnectionPoint.getVirtual_link_reference().equals(vlr.getName())) {
                                boolean networkExists = false;
                                for (Network network : vimInstance.getNetworks()) {
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
                                    network = networkManagement.add(vimInstance, network);
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
        }

        NSRUtils.setDependencies(networkServiceDescriptor, networkServiceRecord);

        networkServiceRecord = nsrRepository.save(networkServiceRecord);


        /**
         * now check for the requires pointing to the nfvo
         */
        //TODO check where to put this
        if (networkServiceRecord.getVnfr() != null)
            for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {
                for (ConfigurationParameter configurationParameter : virtualNetworkFunctionRecord.getRequires().getConfigurationParameters()) {
                    log.debug("Checking parameter: " + configurationParameter.getConfKey());
                    if (configurationParameter.getConfKey().startsWith("nfvo:")) { //the parameters known from the nfvo
                        String[] params = configurationParameter.getConfKey().split("\\:");
                        for (ConfigurationParameter configurationParameterSystem : configurationManagement.queryByName("system").getConfigurationParameters()) {
                            if (configurationParameterSystem.getConfKey().equals(params[1])) {
                                log.debug("Found parameter: " + configurationParameterSystem);
                                configurationParameter.setValue(configurationParameterSystem.getValue());
                            }
                        }
                    }
                }
            }

        vnfmManager.deploy(networkServiceDescriptor, networkServiceRecord);

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
    public void executeAction(NFVMessage nfvMessage, String nsrId, String idVnf, String idVdu, String idVNFCI) throws NotFoundException {

        log.info("Executing action: " + nfvMessage.getAction() + " on VNF with id: " + idVnf);

        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(nsrId);
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = getVirtualNetworkFunctionRecord(idVnf, networkServiceRecord);

        VirtualDeploymentUnit virtualDeploymentUnit = getVirtualDeploymentUnit(idVdu, virtualNetworkFunctionRecord);
        VNFCInstance vnfcInstance = getVNFCInstance(idVNFCI, virtualDeploymentUnit);
        switch (nfvMessage.getAction()) {
            case HEAL:
                // Note: when we get a HEAL message from the API, it contains only the cause (no vnfr or vnfcInstance).
                // Here the vnfr and the vnfcInstance are set into the message, since they are updated.
                VnfmOrHealedMessage VnfmOrHealVNFRequestMessage = (VnfmOrHealedMessage) nfvMessage;
                log.debug("Received Heal message: " + VnfmOrHealVNFRequestMessage);
                VnfmOrHealVNFRequestMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
                VnfmOrHealVNFRequestMessage.setVnfcInstance(vnfcInstance);
                vnfmManager.sendMessageToVNFR(virtualNetworkFunctionRecord, VnfmOrHealVNFRequestMessage);
                break;
        }
    }

    @Override
    public NetworkServiceRecord query(String id) {
        return nsrRepository.findFirstById(id);
    }

    @Override
    public void delete(String id) throws VimException, NotFoundException, InterruptedException, ExecutionException, WrongStatusException {
        log.info("Removing NSR with id: " + id);
        NetworkServiceRecord networkServiceRecord = nsrRepository.findFirstById(id);
        if (networkServiceRecord == null) {
            throw new NotFoundException("NetworkServiceRecord with id " + id + " was not found");
        }

        if (deleteInAllStatus != null && !deleteInAllStatus.equals("") && !Boolean.parseBoolean(deleteInAllStatus)) {
            if (networkServiceRecord.getStatus().ordinal() == Status.NULL.ordinal())
                throw new WrongStatusException("The NetworkService " + networkServiceRecord.getId() + " is in the wrong state. ( Status= " + networkServiceRecord.getStatus() + " )");
            if (networkServiceRecord.getStatus().ordinal() != Status.ACTIVE.ordinal() && networkServiceRecord.getStatus().ordinal() != Status.ERROR.ordinal())
                throw new WrongStatusException("The NetworkService " + networkServiceRecord.getId() + " is in the wrong state. ( Status= " + networkServiceRecord.getStatus() + " )");
        }

        if (networkServiceRecord.getVnfr().size() > 0) {
            networkServiceRecord.setStatus(Status.TERMINATED); // TODO maybe terminating?
            for (VirtualNetworkFunctionRecord virtualNetworkFunctionRecord : networkServiceRecord.getVnfr()) {
                if (removeAfterTimeout) {
                    VNFRTerminator terminator = new VNFRTerminator();
                    terminator.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
                    this.asyncExecutor.submit(terminator);
                }
                vnfmManager.release(virtualNetworkFunctionRecord);
            }
        } else
            nsrRepository.delete(networkServiceRecord.getId());
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @ConfigurationProperties
    class VNFRTerminator implements Runnable {


        private VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;

        public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord() {
            return virtualNetworkFunctionRecord;
        }

        public void setVirtualNetworkFunctionRecord(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {
            this.virtualNetworkFunctionRecord = virtualNetworkFunctionRecord;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeout * 1000);
                if (vnfrRepository.exists(virtualNetworkFunctionRecord.getId()))
                    vnfmManager.terminate(virtualNetworkFunctionRecord);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
