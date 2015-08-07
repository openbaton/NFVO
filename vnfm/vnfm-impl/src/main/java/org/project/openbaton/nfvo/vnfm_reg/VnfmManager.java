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

package org.project.openbaton.nfvo.vnfm_reg;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.Status;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.catalogue.util.EventFinishEvent;
import org.project.openbaton.nfvo.core.interfaces.ConfigurationManagement;
import org.project.openbaton.nfvo.core.interfaces.DependencyManagement;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.exceptions.VimException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * Created by lto on 08/07/15.
 */
@Service
@Scope("singleton")
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class VnfmManager implements org.project.openbaton.vnfm.interfaces.manager.VnfmManager, ApplicationEventPublisherAware, ApplicationListener<EventFinishEvent>, CommandLineRunner {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    private ApplicationEventPublisher publisher;

    private ThreadPoolTaskExecutor asyncExecutor;

    private SyncTaskExecutor serialExecutor;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private DependencyManagement dependencyManagement;

    @Autowired
    private ConfigurationManagement configurationManagement;

    @Autowired
    private ResourceManagement resourceManagement;

    @Autowired
    private VNFLifecycleOperationGranting lifecycleOperationGranting;

    @Autowired
    @Qualifier("VNFRRepository")
    private GenericRepository<VirtualNetworkFunctionRecord> vnfrRepository;

    @Autowired
    @Qualifier("NSRRepository")
    private GenericRepository<NetworkServiceRecord> nsrRepository;

    @Override
    public void init() {
        /**
         * Asynchronous thread executor configuration
         */
        Configuration system = null;
        try {
            system = configurationManagement.queryByName("system");
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        this.asyncExecutor = new ThreadPoolTaskExecutor();
        this.asyncExecutor.setMaxPoolSize(30);
        this.asyncExecutor.setQueueCapacity(30);
        this.asyncExecutor.setKeepAliveSeconds(30);

        for (ConfigurationParameter configurationParameter : system.getConfigurationParameters()){
            if (configurationParameter.getConfKey().equals("vmanager-executor-max-pool-size")) {
                this.asyncExecutor.setMaxPoolSize(Integer.parseInt(configurationParameter.getValue()));
            }
            if (configurationParameter.getConfKey().equals("vmanager-executor-queue-capacity")) {
                this.asyncExecutor.setQueueCapacity(Integer.parseInt(configurationParameter.getValue()));
            }
            if (configurationParameter.getConfKey().equals("vmanager-keep-alive")) {
                this.asyncExecutor.setKeepAliveSeconds(Integer.parseInt(configurationParameter.getValue()));
            }

        }

        this.asyncExecutor.initialize();

        this.serialExecutor = new SyncTaskExecutor();

    }

    @Override
    @Async
    public Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException {
        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
            CoreMessage coreMessage = new CoreMessage();
            coreMessage.setAction(Action.INSTANTIATE);
            coreMessage.setPayload(vnfr);

            VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(vnfr.getEndpoint());
            if (endpoint == null) {
                throw new NotFoundException("VnfManager of type " + vnfr.getType() + " (endpoint = " + vnfr.getEndpoint() + ") is not registered");
            }

            /**
             *  TODO Here use an abstraction to call the particular vnfm_reg
             */
            VnfmSender vnfmSender;
            try {
                vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
            } catch (BeansException e) {
                throw new NotFoundException(e);
            }

            vnfmSender.sendCommand(coreMessage, endpoint);
        }
        return new AsyncResult<Void>(null);
    }

    @Override
    public VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException{
        String senderName = endpointType.toString().toLowerCase() + "Sender";
        return (VnfmSender) this.context.getBean(senderName);
    }

    @Override
    public void executeAction(CoreMessage message) throws VimException, NotFoundException {

//        for (String s: context.getBeanDefinitionNames()){
//            log.debug(s);
//        }

        String beanName = message.getAction().toString().replace("_", "").toLowerCase() + "Task";
        log.debug("Looking for bean called: " + beanName);
        AbstractTask task = (AbstractTask) context.getBean(beanName);

        task.setAction(message.getAction());
        task.setVirtualNetworkFunctionRecord(message.getPayload());

        if (task.isAsync()){
            asyncExecutor.submit(task);
        }else
            serialExecutor.execute(task);

    }

//    public void executeTask(CoreMessage message) throws VimException, NotFoundException {
//        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = null;
//        VnfmSender vnfmSender;
//        try {
//            vnfmSender = this.getVnfmSender(EndpointType.JMS);// we know it is jms, I'm in a jms receiver...
//        } catch (BeansException e2) {
//            throw new NotFoundException(e2);
//        }
//        switch (message.getAction()) {
//            case GRANT_OPERATION:
//                virtualNetworkFunctionRecord = message.getPayload();
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                if (lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord)) {
//                    LifecycleEvent lifecycleEvent = new LifecycleEvent();
//                    lifecycleEvent.setEvent(Event.GRANTED);
//                    if (virtualNetworkFunctionRecord.getLifecycle_event_history() == null)
//                        virtualNetworkFunctionRecord.setLifecycle_event_history(new HashSet<LifecycleEvent>());
//                    virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
//                    message.setPayload(virtualNetworkFunctionRecord);
//                    log.debug("Verison is: " + virtualNetworkFunctionRecord.getHb_version());
//                    vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
//                } else {
//                    message.setAction(Action.ERROR);
//                    vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
//                }
//                break;
//            case ERROR:
//                virtualNetworkFunctionRecord = message.getPayload();
//                log.error("----> ERROR for VNFR: " + virtualNetworkFunctionRecord.getName());
//                virtualNetworkFunctionRecord.setStatus(Status.ERROR);
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                break;
//            case RELEASE_RESOURCES:
//                virtualNetworkFunctionRecord = message.getPayload();
//                log.debug("Released resources for VNFR: " + virtualNetworkFunctionRecord.getName());
//                virtualNetworkFunctionRecord.setStatus(Status.TERMINATED);
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//
//                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
//                    log.debug("Removing VDU: " + virtualDeploymentUnit.getHostname());
//                    this.resourceManagement.release(virtualDeploymentUnit);
//                }
//
//                break;
//            case ALLOCATE_RESOURCES:
//                log.debug("NFVO: ALLOCATE_RESOURCES");
//                virtualNetworkFunctionRecord = message.getPayload();
//                List<String> ids = new ArrayList<>();
//                boolean error = false;
//                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu())
//                    try {
//                        ids.add(resourceManagement.allocate(vdu, virtualNetworkFunctionRecord));
//                    } catch (VimException e) {
//                        e.printStackTrace();
//                        log.error(e.getMessage());
//                        CoreMessage errorMessage = new CoreMessage();
//                        errorMessage.setAction(Action.ERROR);
//                        LifecycleEvent lifecycleEvent = new LifecycleEvent();
//                        lifecycleEvent.setEvent(Event.ERROR);
//                        virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
//                        errorMessage.setPayload(virtualNetworkFunctionRecord);
//                        vnfmSender.sendCommand(errorMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
//                        return;
//                    } catch (VimDriverException e) {
//                        e.printStackTrace();
//                        log.error(e.getMessage());
//                        message.setAction(Action.ERROR);
//                        LifecycleEvent lifecycleEvent = new LifecycleEvent();
//                        lifecycleEvent.setEvent(Event.ERROR);
//                        virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
//                        virtualNetworkFunctionRecord.setStatus(Status.ERROR);
//                        virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                        message.setPayload(virtualNetworkFunctionRecord);
//                        vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
//                        error = true;
////                        return;
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
//
//                for (LifecycleEvent event : virtualNetworkFunctionRecord.getLifecycle_event()) {
//                    if (event.getEvent().ordinal() == Event.ALLOCATE.ordinal()) {
//                        virtualNetworkFunctionRecord.getLifecycle_event_history().add(event);
//                        virtualNetworkFunctionRecord.getLifecycle_event().remove(event);
//                        break;
//                    }
//                }
//
//                if (!error) {
//                    CoreMessage coreMessage = new CoreMessage();
//                    coreMessage.setAction(Action.INSTANTIATE);
//                    coreMessage.setPayload(virtualNetworkFunctionRecord);
//                    vnfmSender.sendCommand(coreMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint()));
//                }
//                break;
//            case INSTANTIATE:
//                log.debug("NFVO: instantiate finish");
//                virtualNetworkFunctionRecord = message.getPayload();
//                log.trace("Verison is: " + virtualNetworkFunctionRecord.getHb_version());
//                virtualNetworkFunctionRecord.setStatus(Status.INACTIVE);
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                message.setPayload(virtualNetworkFunctionRecord);
//                log.info("Instantiation is finished for vnfr: " + virtualNetworkFunctionRecord.getName());
//                log.debug("Calling dependency management for VNFR: " + virtualNetworkFunctionRecord.getName());
//                int dep = 0;
//                try {
//                    dep = dependencyManagement.provisionDependencies(virtualNetworkFunctionRecord);
//                } catch (NotFoundException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                if (dep == 0) {
//                    log.info("VNFR: " + virtualNetworkFunctionRecord.getName() + " (" + virtualNetworkFunctionRecord.getId() + ") has 0 dependencies, setting status to ACTIVE");
//                    virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
//                    virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                    message.setPayload(virtualNetworkFunctionRecord);
//                }
//                break;
//            case MODIFY:
//                log.debug("NFVO: MODIFY finish");
//                virtualNetworkFunctionRecord = message.getPayload();
//                log.trace("VNFR Verison is: " + virtualNetworkFunctionRecord.getHb_version());
//                virtualNetworkFunctionRecord.setStatus(Status.ACTIVE);
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                log.debug("VNFR Status is: " + virtualNetworkFunctionRecord.getStatus());
//                break;
//            case RELEASE_RESOURCES_FINISH:
//                virtualNetworkFunctionRecord = message.getPayload();
//                log.debug("Released resources for VNFR: " + virtualNetworkFunctionRecord.getName());
//                virtualNetworkFunctionRecord.setStatus(Status.TERMINATED);
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                break;
//            case SCALING:
//                log.debug("NFVO: SCALING");
//                VirtualNetworkFunctionRecord scalingVirtualNetworkFunctionRecord = message.getPayload();
//                virtualNetworkFunctionRecord = vnfrRepository.find(scalingVirtualNetworkFunctionRecord.getId());
//                virtualNetworkFunctionRecord.setStatus(scalingVirtualNetworkFunctionRecord.getStatus());
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                break;
//            case SCALE_UP_FINISHED:
//                log.debug("NFVO: SCALE_UP_FINISHED");
//                VirtualNetworkFunctionRecord scaledUpVirtualNetworkFunctionRecord = message.getPayload();
//                virtualNetworkFunctionRecord = vnfrRepository.find(scaledUpVirtualNetworkFunctionRecord.getId());
//                virtualNetworkFunctionRecord.setStatus(scaledUpVirtualNetworkFunctionRecord.getStatus());
//                List<String> existingVDUs = new ArrayList<String>();
//                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
//                    existingVDUs.add(vdu.getId());
//                }
//                for (VirtualDeploymentUnit vdu : scaledUpVirtualNetworkFunctionRecord.getVdu()) {
//                    if (!existingVDUs.contains(vdu.getId())) {
//                        virtualNetworkFunctionRecord.getVdu().add(vdu);
//                    }
//                }
//                Set<String> new_addresses = new HashSet<>();
//                for (String ip : scaledUpVirtualNetworkFunctionRecord.getVnf_address()) {
//                    if (!virtualNetworkFunctionRecord.getVnf_address().contains(ip)) {
//                        new_addresses.add(ip);
//                    }
//                }
//                virtualNetworkFunctionRecord.getVnf_address().addAll(new_addresses);
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                break;
//            case SCALE_DOWN_FINISHED:
//                log.debug("NFVO: SCALE_DOWN_FINISHED");
//                VirtualNetworkFunctionRecord scaledDownVirtualNetworkFunctionRecord = message.getPayload();
//                virtualNetworkFunctionRecord = vnfrRepository.find(scaledDownVirtualNetworkFunctionRecord.getId());
//                virtualNetworkFunctionRecord.setStatus(scaledDownVirtualNetworkFunctionRecord.getStatus());
//                existingVDUs = new ArrayList<String>();
//                for (VirtualDeploymentUnit vdu : scaledDownVirtualNetworkFunctionRecord.getVdu()) {
//                    existingVDUs.add(vdu.getId());
//                }
//                Set<VirtualDeploymentUnit> removedVdus = new HashSet<>();
//                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
//                    if (!existingVDUs.contains(vdu.getId())) {
//                        removedVdus.add(vdu);
//                    }
//                }
//                virtualNetworkFunctionRecord.getVdu().removeAll(removedVdus);
//                Set<String> old_addresses = new HashSet<>();
//                for (String ip : virtualNetworkFunctionRecord.getVnf_address()) {
//                    if (!scaledDownVirtualNetworkFunctionRecord.getVnf_address().contains(ip)) {
//                        old_addresses.add(ip);
//                    }
//                }
//                virtualNetworkFunctionRecord.getVnf_address().removeAll(old_addresses);
//                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
//                break;
//        }
//
//        publishEvent(message);
//
//        findAndSetNSRStatus(virtualNetworkFunctionRecord);
//    }

    private void findAndSetNSRStatus(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) {

        if (virtualNetworkFunctionRecord == null)
            return;

        log.debug("The nsr id is: " + virtualNetworkFunctionRecord.getParent_ns_id());

        Status status = Status.TERMINATED;
        NetworkServiceRecord networkServiceRecord;
        try {
            networkServiceRecord = nsrRepository.find(virtualNetworkFunctionRecord.getParent_ns_id());
        } catch (NoResultException e) {
            log.error("No NSR found with id " + virtualNetworkFunctionRecord.getParent_ns_id());
            return;
        }
        log.debug("Checking the status of NSR: " + networkServiceRecord.getName());

        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
            log.debug("VNFR " + vnfr.getName() + " is in state: " + vnfr.getStatus());
            if (status.ordinal() > vnfr.getStatus().ordinal()) {
                status = vnfr.getStatus();
            }
        }

        log.debug("Setting NSR status to: " + status);
        networkServiceRecord.setStatus(status);
        networkServiceRecord = nsrRepository.merge(networkServiceRecord);
//        log.debug("Now the status is: " + networkServiceRecord.getStatus());
        if (status.ordinal() == Status.ACTIVE.ordinal())
            publishEvent(Action.INSTANTIATE_FINISH, networkServiceRecord);
        else if (status.ordinal() == Status.TERMINATED.ordinal()) {
            publishEvent(Action.RELEASE_RESOURCES_FINISH, networkServiceRecord);
            nsrRepository.remove(networkServiceRecord);
        }
    }

    private void publishEvent(CoreMessage message) {
        publishEvent(message.getAction(), message.getPayload());
    }

    private void publishEvent(Action action, Serializable payload) {
        ApplicationEventNFVO event = new ApplicationEventNFVO(this, action, payload);
        log.debug("Publishing event: " + event);
        publisher.publishEvent(event);
    }

    @Override
    @Async
    public Future<Void> modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, CoreMessage coreMessage) throws NotFoundException {
        VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(virtualNetworkFunctionRecordDest.getEndpoint());
        if (endpoint == null) {
            throw new NotFoundException("VnfManager of type " + virtualNetworkFunctionRecordDest.getType() + " (endpoint = " + virtualNetworkFunctionRecordDest.getEndpoint() + ") is not registered");
        }
        VnfmSender vnfmSender;
        try {

            vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
        } catch (BeansException e) {
            throw new NotFoundException(e);
        }

        log.debug("Sending message " + coreMessage.getAction() + " to endpoint " + endpoint);
        vnfmSender.sendCommand(coreMessage, endpoint);
        return new AsyncResult<Void>(null);
    }

    @Override
    @Async
    public Future<Void> release(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException {
        VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getEndpoint());
        if (endpoint == null) {
            throw new NotFoundException("VnfManager of type " + virtualNetworkFunctionRecord.getType() + " (endpoint = " + virtualNetworkFunctionRecord.getEndpoint() + ") is not registered");
        }
        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(Action.RELEASE_RESOURCES);
        coreMessage.setPayload(virtualNetworkFunctionRecord);
        VnfmSender vnfmSender;
        try {

            vnfmSender = this.getVnfmSender(endpoint.getEndpointType());
        } catch (BeansException e) {
            throw new NotFoundException(e);
        }

        vnfmSender.sendCommand(coreMessage, endpoint);
        return new AsyncResult<Void>(null);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Override
    public void onApplicationEvent(EventFinishEvent event) {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = event.getVirtualNetworkFunctionRecord();
        publishEvent(event.getAction(), virtualNetworkFunctionRecord);
        findAndSetNSRStatus(virtualNetworkFunctionRecord);
    }

    @Override
    public void run(String... args) throws Exception {
        init();
    }
}
