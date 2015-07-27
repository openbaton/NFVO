package org.project.openbaton.common.vnfm_sdk.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JndiDestinationResolver;

import javax.annotation.PreDestroy;
import javax.jms.*;
import java.io.Serializable;

/**
 * Created by lto on 28/05/15.
 */

@SpringBootApplication
@EnableJms
public abstract class AbstractVnfmSpringJMS extends AbstractVnfm implements CommandLineRunner {

    @Autowired
    protected JmsListenerContainerFactory topicJmsContainerFactory;

    private boolean exit = false;

    protected String SELECTOR;
    private VnfmManagerEndpoint vnfmManagerEndpoint;

    public String getSELECTOR() {
        return SELECTOR;
    }

    public void setSELECTOR(String SELECTOR) {
        this.SELECTOR = SELECTOR;
    }

    @Autowired
    private JmsTemplate jmsTemplate;

    @PreDestroy
    private void shutdown(){
        log.debug("PREDESTROY");
        this.unregister(vnfmManagerEndpoint);
    }

    @Override
    protected void unregister(VnfmManagerEndpoint endpoint) {
        this.sendMessageToQueue("vnfm-unregister", endpoint);
    }

    @Bean
    ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory();
    }

    @Bean
    DestinationResolver destinationResolver() {
        return new JndiDestinationResolver();
    }


    @Bean
    JmsListenerContainerFactory<?> jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_AUTO");
        factory.setConnectionFactory(connectionFactory());
        factory.setDestinationResolver(destinationResolver());
        factory.setConcurrency("5");
        return factory;
    }

//    @Bean
//    JmsListenerContainerFactory<?> topicJmsContainerFactory(ConnectionFactory connectionFactory) {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        factory.setCacheLevelName("CACHE_CONNECTION");
//        factory.setConnectionFactory(connectionFactory);
//        factory.setConcurrency("1");
//        factory.setPubSubDomain(true);
//        factory.setSubscriptionDurable(true);
//        factory.setClientId(""+ Thread.currentThread().getId());
//        return factory;
//    }

    //    @JmsListener(destination = "core-vnfm-actions", containerFactory = "queueJmsContainerFactory")
    private void onMessage() throws JMSException {

        Message msg = jmsTemplate.receive("core-" + this.type + "-actions");

        CoreMessage message = (CoreMessage) ((ObjectMessage)msg).getObject();
        log.trace("VNFM-DUMMY: received " + message);
        this.onAction(message);
    }

    private CoreMessage receiveCoreMessage(String destination, String selector) throws JMSException {
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setPubSubNoLocal(true);
        CoreMessage message = (CoreMessage) ((ObjectMessage) jmsTemplate.receiveSelected(destination, "type = \'" + selector + "\'")).getObject();
        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setPubSubNoLocal(false);
        return message;
    }

    protected Serializable sendAndReceiveMessage(String receiveFromQueueName, String sendToQueueName, final Serializable vduMessage) throws JMSException {
        sendMessageToQueue(sendToQueueName, vduMessage);
        ObjectMessage objectMessage = (ObjectMessage) jmsTemplate.receive(receiveFromQueueName);
        log.debug("Received: " + objectMessage.getObject());
//        VDUMessage answer = (VDUMessage) objectMessage.getObject();
//        if (answer.getLifecycleEvent().ordinal() != Event.ERROR.ordinal()){
//            return true;
//        }
        return objectMessage.getObject();
    }

    protected String sendAndReceiveStringMessage(String receiveFromQueueName, String sendToQueueName, final String stringMessage) throws JMSException {
        log.debug("Sending message: " + stringMessage + " to Queue: " + sendToQueueName);
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(stringMessage);
                return textMessage;
            }
        };
        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setPubSubNoLocal(false);

        jmsTemplate.send(sendToQueueName, messageCreator);
        TextMessage textMessage = (TextMessage) jmsTemplate.receive(receiveFromQueueName);

        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setPubSubNoLocal(true);
        String answer = textMessage.getText();
        log.debug("Received: " + answer);
        // check errors
        /*if (answer.equals("error")){
            return null;
        }*/
        return answer;
    }

    protected void sendMessageToQueue(String sendToQueueName, final Serializable vduMessage) {
        log.debug("Sending message: " + vduMessage + " to Queue: " + sendToQueueName);
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage(vduMessage);
                return objectMessage;
            }
        };
        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setPubSubNoLocal(false);
        jmsTemplate.send(sendToQueueName, messageCreator);
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setPubSubNoLocal(true);
    }

    @Override
    public void run(String... args) throws Exception {
        //TODO initialize and register

        loadProperties();
        this.setSELECTOR(this.getEndpoint());
        log.debug("SELECTOR: " + this.getEndpoint());

        vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        vnfmManagerEndpoint.setEndpointType(EndpointType.JMS);

        log.debug("Registering to queue: vnfm-register");
        sendMessageToQueue("vnfm-register", vnfmManagerEndpoint);

        try {
            while (true)
                onMessage();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
