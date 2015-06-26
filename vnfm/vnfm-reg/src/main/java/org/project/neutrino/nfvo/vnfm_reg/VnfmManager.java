package org.project.neutrino.nfvo.vnfm_reg;

import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.Action;
import org.project.neutrino.nfvo.catalogue.nfvo.ApplicationEventNFVO;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.common.exceptions.VimException;
import org.project.neutrino.nfvo.common.vnfm.utils.UtilsJMS;
import org.project.neutrino.nfvo.core.interfaces.ResourceManagement;
import org.project.neutrino.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
@Service
@Scope
public class VnfmManager implements org.project.neutrino.vnfm.interfaces.manager.VnfmManager, ApplicationEventPublisherAware {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    @Qualifier("vnfmRegister")
    private org.project.neutrino.vnfm.interfaces.register.VnfmRegister vnfmRegister;

    private ApplicationEventPublisher publisher;

    @Autowired
    private ResourceManagement resourceManagement;

    @Override
    @Async
    public Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException, NamingException, JMSException {
        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
            CoreMessage coreMessage = new CoreMessage();
            coreMessage.setAction(Action.INSTATIATE);
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

    @Override
    @JmsListener(destination = "vnfm-core-actions", containerFactory = "queueJmsContainerFactory")
    public void actionFinished(@Payload CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException {
        log.debug("Received: " + coreMessage);

        try {
            this.executeAction(coreMessage);
        } catch (VimException e) {
            log.error(e.getMessage());
            VnfmSender vnfmSender;
            try {

                vnfmSender = this.getVnfmSender("jms");// we know it is jms, I'm in a jms receiver...
            } catch (BeansException e2) {
                throw new NotFoundException(e2);
            }

            CoreMessage errorMessage = new CoreMessage();
            errorMessage.setAction(Action.ERROR);
            errorMessage.setPayload("There was an error while deploying VMs");
            vnfmSender.sendCommand(errorMessage, vnfmRegister.getVnfm(((VirtualNetworkFunctionRecord) coreMessage.getPayload()).getType()));
        }
    }

    @Override
    public VnfmSender getVnfmSender(String endpointType) throws BeansException{
        String senderName = endpointType + "Sender";
        return (VnfmSender) this.context.getBean(senderName);
    }

    @Override
    public void executeAction(CoreMessage message) throws VimException, JMSException, NamingException {
        VirtualNetworkFunctionRecord virtualNetworkFunctionRecord;
        switch (message.getAction()){

            case INSTATIATE_FINISH:
                ApplicationEventNFVO event = new ApplicationEventNFVO(this, message.getAction());
                log.debug("Publishing event: " + event);
                publisher.publishEvent(event);
                break;
            case RELEASE_RESOURCES:
                virtualNetworkFunctionRecord = (VirtualNetworkFunctionRecord) message.getPayload();
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu())
                    resourceManagement.release(vdu);
                break;
            case ALLOCATE_RESOURCES:
                virtualNetworkFunctionRecord = (VirtualNetworkFunctionRecord) message.getPayload();
                List<Future<String>> ids = new ArrayList<>();
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu())
                    ids.add(resourceManagement.allocate(vdu, virtualNetworkFunctionRecord));

                CoreMessage coreMessage = new CoreMessage();
                coreMessage.setAction(Action.ALLOCATE_RESOURCES);
                coreMessage.setPayload(ids.toString());

                UtilsJMS.sendToQueue(coreMessage, "core-vnfm-actions");

                break;
            case INSTATIATE:
                break;
        }
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
