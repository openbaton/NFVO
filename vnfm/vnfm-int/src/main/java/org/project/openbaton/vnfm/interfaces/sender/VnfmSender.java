package org.project.openbaton.vnfm.interfaces.sender;

import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 03/06/15.
 */
public interface VnfmSender {
    void sendCommand(final CoreMessage coreMessage, VnfmManagerEndpoint endpoint) throws JMSException, NamingException;

}
