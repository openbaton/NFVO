package org.project.neutrino.vnfm.interfaces.sender;

import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 03/06/15.
 */
public interface VnfmSender {
    void sendCommand(final CoreMessage coreMessage, VnfmManagerEndpoint endpoint) throws JMSException, NamingException;

    void sendToTopic(CoreMessage coreMessage, String destinationQueueName, String selector);
}
