package org.project.openbaton.nfvo.vnfm_reg.impl.sender;

import org.project.openbaton.nfvo.catalogue.nfvo.CoreMessage;
import org.project.openbaton.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 03/06/15.
 */
@Service(value = "restSender")
public class RestSender implements VnfmSender{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void sendCommand(final CoreMessage coreMessage, VnfmManagerEndpoint endpoint) throws JMSException, NamingException {
//        log.debug("Sending message: " + coreMessage + " to Queue: vnfm-actions where selector is: type=\'" + endpoint.getEndpoint() + "\'");
        this.sendToTopic(coreMessage,"queue-name", endpoint.getEndpoint());
    }

    @Override
    public void sendToTopic(CoreMessage coreMessage, String destinationQueueName, String selector) {
        log.debug("Sending message: " + coreMessage + " to Queue: vnfm-actions where selector is: type=\'" + selector + "\'");
    }
}
