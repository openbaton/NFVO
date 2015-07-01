package org.project.neutrino.vnfm.interfaces.manager;

import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.EndpointType;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.common.exceptions.VimException;
import org.project.neutrino.vnfm.interfaces.sender.VnfmSender;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmManager {
    Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException, NamingException, JMSException;

    @JmsListener(destination = "vnfm-core-actions", containerFactory = "myJmsContainerFactory")
    void actionFinished(@Payload CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException;

    VnfmSender getVnfmSender(EndpointType endpointType);

    void executeAction(CoreMessage message) throws VimException, JMSException, NamingException, NotFoundException;

    @Async
    Future<Void> modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException;

    Future<Void> release(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException, NamingException, JMSException;
}
