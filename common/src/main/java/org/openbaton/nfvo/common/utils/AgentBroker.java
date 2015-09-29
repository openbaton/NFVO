package org.openbaton.nfvo.common.utils;

import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.interfaces.Receiver;
import org.openbaton.nfvo.common.interfaces.Sender;
import org.openbaton.nfvo.common.utils.jms.JmsReceiver;
import org.openbaton.nfvo.common.utils.jms.JmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by tce on 14.08.15.
 */
@Service
@Scope
public class AgentBroker {

    @Autowired
    private ConfigurableApplicationContext context;

    public Sender getSender(EndpointType endpointType) throws NotFoundException {
        switch (endpointType) {
            case JMS:
                return (JmsSender) context.getBean("jmsSender");
            case REST:
                //TODO RestSender
            default:
                throw new NotFoundException("no type sender found");
        }
    }

    public Receiver getReceiver(EndpointType endpointType) throws NotFoundException {
        switch (endpointType) {
            case JMS:
                return (JmsReceiver) context.getBean("jmsReceiver");
            case REST:
                //TODO RestReceiver
            default:
                throw new NotFoundException("no type Receiver found");
        }
    }
}
