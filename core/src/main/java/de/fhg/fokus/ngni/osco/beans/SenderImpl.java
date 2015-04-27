package de.fhg.fokus.ngni.osco.beans;

/**
 * Created by lto on 17/04/15.
 */

import de.fhg.fokus.ngni.osco.interfaces.Sender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Service
public class SenderImpl implements Sender {

    private org.slf4j.Logger log = LoggerFactory.getLogger(MyBean.class);

    @Autowired
    ConfigurableApplicationContext context;

    private MessageCreator messageCreator = new MessageCreator() {
        @Override
        public Message createMessage(Session session) throws JMSException {
            return session.createTextMessage("ping!");
        }
    };

//    @Bean
    public void simpleSend(){
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        log.info("Sending a new message.");
        jmsTemplate.send("mailbox-destination", messageCreator);;
        log.info(context.getId());
    }
}
