package org.openbaton.nfvo.common.utils.jms;

import org.openbaton.nfvo.common.interfaces.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.io.Serializable;

/**
 * Created by tce on 13.08.15.
 */
@Service
@Scope
public class JmsSender implements Sender {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void send(String destination, final Serializable message) {
        log.trace("Sending message: " + message + " to Queue: " + destination);
        MessageCreator messageCreator = getMessageCreator(message);
        jmsTemplate.send(destination, messageCreator);
    }

    public void send(Destination destination, final Serializable message) {
        log.trace("Sending message: " + message + " to Queue: " + destination);
        MessageCreator messageCreator = getMessageCreator(message);
        jmsTemplate.send(destination, messageCreator);
    }

    private MessageCreator getMessageCreator(final Serializable message) {
        return new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message msg;
                if (message instanceof java.lang.String)
                    msg = session.createTextMessage((String) message);
                else
                    msg = session.createObjectMessage(message);
                return msg;
            }
        };
    }

    @Override
    public Serializable receiveObject(String destination) throws JMSException {
        return ((ObjectMessage) jmsTemplate.receive(destination)).getObject();
    }

    @Override
    public String receiveText(String destination) throws JMSException {
        return ((TextMessage) jmsTemplate.receive(destination)).getText();
    }
}
