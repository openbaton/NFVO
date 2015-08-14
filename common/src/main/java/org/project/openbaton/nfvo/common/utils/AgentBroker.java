package org.project.openbaton.nfvo.common.utils;

import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.interfaces.Receiver;
import org.project.openbaton.nfvo.common.interfaces.Sender;
import org.project.openbaton.nfvo.common.interfaces.concretes.JmsSender;
import org.project.openbaton.nfvo.common.interfaces.concretes.JmsReceiver;
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
                //return (RestSender) context.getBean("restSender"); TODO RestSender
            default:
                throw new NotFoundException("no type sender found");
        }
    }

    public Receiver getReceiver(EndpointType endpointType) throws NotFoundException {
        switch (endpointType) {
            case JMS:
                return (JmsReceiver) context.getBean("jmsReceiver");
            case REST:
                //return (RestReceiver) context.getBean("restReceiver"); TODO RestReceiver
            default:
                throw new NotFoundException("no type Receiver found");
        }
    }
}
