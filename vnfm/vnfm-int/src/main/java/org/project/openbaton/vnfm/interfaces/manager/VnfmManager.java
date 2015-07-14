package org.project.openbaton.vnfm.interfaces.manager;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.nfvo.exceptions.NotFoundException;
import org.project.openbaton.nfvo.exceptions.VimException;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.springframework.scheduling.annotation.Async;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmManager {
    Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException, NamingException, JMSException;

    VnfmSender getVnfmSender(EndpointType endpointType);

    void executeAction(CoreMessage message) throws VimException, JMSException, NamingException, NotFoundException;

    @Async
    Future<Void> modify(VirtualNetworkFunctionRecord virtualNetworkFunctionRecordDest, CoreMessage coreMessage) throws NotFoundException, NamingException, JMSException;

    Future<Void> release(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException, NamingException, JMSException;
}
