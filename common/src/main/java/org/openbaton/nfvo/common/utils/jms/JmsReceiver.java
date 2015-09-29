package org.openbaton.nfvo.common.utils.jms;

import org.openbaton.nfvo.common.interfaces.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import java.io.Serializable;

/**
 * Created by tce on 14.08.15.
 */
@Service
@Scope
public class JmsReceiver implements Receiver {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public Serializable receive(String destination, String selector) throws JMSException {
        Message message = jmsTemplate.receiveSelected(destination, selector);
        if (message instanceof ObjectMessage)
            return ((ObjectMessage) message).getObject();
        else
            return ((TextMessage) message).getText();
    }
}
