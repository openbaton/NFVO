package org.project.neutrino.nfvo.core.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Created by lto on 16/04/15.
 */
@Service
@EnableJms
public class MyBean implements org.project.neutrino.nfvo.core.interfaces.MyBean {

    private Logger log = LoggerFactory.getLogger(MyBean.class);

    @Autowired
    ConfigurableApplicationContext context;

    @Override
    @Scope("singleton")
    public String myBean(){
        return "ciao Pippo";
    }

    @Override
    public String send(){
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("ping!");
            }
        };
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        log.debug("Sending a new message.");
        jmsTemplate.send("mailbox-destination", messageCreator);
        return "ping sent";
    }

    @Override
    @Bean
    public JmsListenerContainerFactory<?> myJmsContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    @Override
    public void close(){
        context.close();
    }
}
