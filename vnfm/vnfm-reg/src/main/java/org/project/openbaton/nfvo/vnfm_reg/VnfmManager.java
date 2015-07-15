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

import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.exceptions.VimException;
import org.project.openbaton.nfvo.core.interfaces.ResourceManagement;
import org.project.openbaton.nfvo.core.interfaces.VNFLifecycleOperationGranting;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 08/07/15.
 */
@Service
@Scope
public class VnfmManager implements org.project.openbaton.vnfm.interfaces.manager.VnfmManager, ApplicationEventPublisherAware {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    @Qualifier("vnfmRegister")
    private VnfmRegister vnfmRegister;

    private ApplicationEventPublisher publisher;

    @Autowired
    private ResourceManagement resourceManagement;

    @Autowired
    private VNFLifecycleOperationGranting lifecycleOperationGranting;

    @Autowired
    @Qualifier("VNFRRepository")
    private GenericRepository<VirtualNetworkFunctionRecord> vnfrRepository;

    @Override
    @Async
    public Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException, NamingException, JMSException {
        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
            CoreMessage coreMessage = new CoreMessage();
            coreMessage.setAction(Action.INSTANTIATE);
            coreMessage.setPayload(vnfr);

            VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(vnfr.getType());
            if (endpoint == null) {
                throw new NotFoundException("VnfManager of type " + vnfr.getType() + " is not registered");
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

//    @Override
//    public abstract void actionFinished(@Payload CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException;

    @Override
    public VnfmSender getVnfmSender(EndpointType endpointType) throws BeansException{
        String senderName = endpointType.toString().toLowerCase() + "Sender";
        return (VnfmSender) this.context.getBean(senderName);
    }

    @Override
    public void executeAction(CoreMessage message) throws JMSException, NamingException, NotFoundException, VimException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
        VnfmSender vnfmSender;
        try {
            vnfmSender = this.getVnfmSender(EndpointType.JMS);// we know it is jms, I'm in a jms receiver...
        } catch (BeansException e2) {
            throw new NotFoundException(e2);
        }
        switch (message.getAction()){
            case GRANT_OPERATION:
                virtualNetworkFunctionRecord = message.getPayload();
                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
                if (lifecycleOperationGranting.grantLifecycleOperation(virtualNetworkFunctionRecord)){
                    vnfmSender.sendCommand(message,vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getType()));
                }else {
                    message.setAction(Action.ERROR);
                    vnfmSender.sendCommand(message, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getType()));
                }
                break;
            case INSTANTIATE_FINISH:
                log.debug("INSTANTIATE_FINISH");
                virtualNetworkFunctionRecord = message.getPayload();
                virtualNetworkFunctionRecord = vnfrRepository.merge(virtualNetworkFunctionRecord);
                log.info("Instantiation is finished for vnfr: " +virtualNetworkFunctionRecord.getName());
                break;
            case RELEASE_RESOURCES:
                virtualNetworkFunctionRecord = (VirtualNetworkFunctionRecord) message.getPayload();
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu())
                    try {
                        resourceManagement.release(vdu);
                    } catch (VimException e) {

                        e.printStackTrace();
                        log.error(e.getMessage());

                        CoreMessage errorMessage = new CoreMessage();
                        errorMessage.setAction(Action.ERROR);
                        LifecycleEvent lifecycleEvent = new LifecycleEvent();
                        lifecycleEvent.setEvent(Event.ERROR);
                        virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
                        errorMessage.setPayload(virtualNetworkFunctionRecord);
                        vnfmSender.sendCommand(errorMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getType()));
                        return;
                    }
                break;
            case ALLOCATE_RESOURCES:
                log.debug("ALLOCATE_RESOURCES");
                virtualNetworkFunctionRecord = (VirtualNetworkFunctionRecord) message.getPayload();
                List<Future<String>> ids = new ArrayList<>();
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu())
                    try {
                        ids.add(resourceManagement.allocate(vdu, virtualNetworkFunctionRecord));
                    } catch (VimException e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                        CoreMessage errorMessage = new CoreMessage();
                        errorMessage.setAction(Action.ERROR);
                        LifecycleEvent lifecycleEvent = new LifecycleEvent();
                        lifecycleEvent.setEvent(Event.ERROR);
                        virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
                        errorMessage.setPayload(virtualNetworkFunctionRecord);
                        vnfmSender.sendCommand(errorMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getType()));
                        return;
                    } catch (VimDriverException e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                        CoreMessage errorMessage = new CoreMessage();
                        errorMessage.setAction(Action.ERROR);
                        LifecycleEvent lifecycleEvent = new LifecycleEvent();
                        lifecycleEvent.setEvent(Event.ERROR);
                        virtualNetworkFunctionRecord.getLifecycle_event_history().add(lifecycleEvent);
                        errorMessage.setPayload(virtualNetworkFunctionRecord);
                        vnfmSender.sendCommand(errorMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getType()));
                        return;
                    }

                CoreMessage coreMessage = new CoreMessage();
                coreMessage.setAction(Action.INSTANTIATE);
                coreMessage.setPayload(virtualNetworkFunctionRecord);

                vnfmSender.sendCommand(coreMessage, vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getType()));
                break;
            case INSTANTIATE:
                break;
        }

        publishEvent(message);
    }

    private void publishEvent(CoreMessage message) {
        ApplicationEventNFVO event = new ApplicationEventNFVO(this, message.getAction(), message.getPayload());
        log.debug("Publishing event: " + event);
        publisher.publishEvent(event);
    }

    @Override
    @Async
    public Future<Void> modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException {
        VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(virtualNetworkFunctionRecordDest.getType());
        if (endpoint == null) {
            throw new NotFoundException("VnfManager of type " + virtualNetworkFunctionRecordDest.getType() + " is not registered");
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
    public Future<Void> release(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException, NamingException, JMSException {
        VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(virtualNetworkFunctionRecord.getType());
        if (endpoint == null) {
            throw new NotFoundException("VnfManager of type " + virtualNetworkFunctionRecord.getType() + " is not registered");
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
}
