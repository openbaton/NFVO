package org.project.neutrino.nfvo.vnfm_reg.impl.sender;

import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.NamingException;

/**
 * Created by lto on 03/06/15.
 */
@Service(value = "jmsSender")
public class JmsSender implements VnfmSender{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("topicJmsContainerFactory")
    private JmsListenerContainerFactory topicJmsContainerFactory;

    @Override
    public void sendCommand(final CoreMessage coreMessage, final VnfmManagerEndpoint endpoint) throws JMSException, NamingException {
        String topicName = "core-vnfm-actions";
        this.sendToTopic(coreMessage,topicName,endpoint.getEndpoint());
    }

    @Override
    public void sendToTopic(final CoreMessage coreMessage, String destinationTopicName, final String selector) {
        log.debug("Sending message: " + coreMessage.getAction() + " to Topic: " + destinationTopicName + " where selector is: type=\'" + selector + "\'");
        log.trace("Sending message: " + coreMessage + " to Topic: " + destinationTopicName + " where selector is: type=\'" + selector + "\'");
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage(coreMessage);
                log.trace("SELECTOR: type=\'"+ selector+ "\'");
                objectMessage.setStringProperty("type", selector );
                return objectMessage;
            }
        };
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setPubSubNoLocal(true);
//        jmsTemplate.setExplicitQosEnabled(true);
//        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.send(destinationTopicName, messageCreator);

    }
}
