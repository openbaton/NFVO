package org.project.openbaton.nfvo.core.events.senders;

import org.project.openbaton.common.catalogue.nfvo.ApplicationEventNFVO;
import org.project.openbaton.common.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.nfvo.core.interfaces.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.concurrent.Future;

/**
 * Created by lto on 01/07/15.
 */
@Service
@Scope
public class JmsEventSender implements EventSender{

    @Autowired
    private JmsTemplate jmsTemplate;
    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    @Async
    public Future<Void> send(EventEndpoint endpoint, final ApplicationEventNFVO event) {

        log.debug("Sending message: " + event + " to endpoint: " + endpoint);
        log.info("Sending message: " + event.getAction() + " to endpoint: " + endpoint.getName());
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage(event);
//                log.trace("SELECTOR: type=\'"+ selector+ "\'");
//                objectMessage.setStringProperty("type", selector );
                return objectMessage;
            }
        };
//        jmsTemplate.setPubSubDomain(true);
//        jmsTemplate.setPubSubNoLocal(true);
//        jmsTemplate.setExplicitQosEnabled(true);
//        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.send(endpoint.getEndpoint(), messageCreator);

        return new AsyncResult<>(null);
    }
}
