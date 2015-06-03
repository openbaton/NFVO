package org.project.neutrino.nfvo.vnfm_reg;

import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.Action;
import org.project.neutrino.nfvo.catalogue.nfvo.ApplicationEventNFVO;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
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

    @Override
    @Async
    public Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException, NamingException, JMSException {
        for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
            VnfmManagerEndpoint endpoint = vnfmRegister.getVnfm(vnfr.getType());
            if (endpoint == null) {
                throw new NotFoundException("VnfManager of type " + vnfr.getType() + " is not registered");
            }

            CoreMessage coreMessage = new CoreMessage();
            coreMessage.setAction(Action.INSTATIATE);
            coreMessage.setObject(vnfr);

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
    @JmsListener(destination = "vnfm-core-actions", containerFactory = "myJmsContainerFactory")
    public void actionFinished(@Payload CoreMessage coreMessage) {
        log.debug("Received: " + coreMessage);
        ApplicationEventNFVO event = new ApplicationEventNFVO(this, coreMessage.getAction());
        log.debug("Publishing event: " + event);
        publisher.publishEvent(event);
    }

    @Override
    public VnfmSender getVnfmSender(String endpointType) throws BeansException{
        String senderName = endpointType + "Sender";

        return (VnfmSender) this.context.getBean(senderName);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
