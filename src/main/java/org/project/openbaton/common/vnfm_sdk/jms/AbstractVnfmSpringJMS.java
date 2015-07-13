package org.project.openbaton.common.vnfm_sdk.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.common.catalogue.mano.common.Event;
import org.project.openbaton.common.catalogue.nfvo.CoreMessage;
import org.project.openbaton.common.catalogue.nfvo.EndpointType;
import org.project.openbaton.common.catalogue.nfvo.VDUMessage;
import org.project.openbaton.common.catalogue.nfvo.VnfmManagerEndpoint;
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
    public void shutdown(){
        log.debug("PREDESTROY");
        this.unregister(vnfmManagerEndpoint);
    }

    @Override
    protected void unregister(VnfmManagerEndpoint endpoint) {
        this.sendMessageToQueue("vnfm-unregister", endpoint);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory();
    }

    @Bean
    JmsListenerContainerFactory<?> topicJmsContainerFactory(ConnectionFactory connectionFactory) {
        log.debug("type=\"" + SELECTOR + "\"");
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
//        factory.setConcurrency("1");
        factory.setPubSubDomain(true);
        factory.setClientId(SELECTOR + "-" + Math.random());
        factory.setSubscriptionDurable(true);
        return factory;
    }

    private void onMessage(String destination, String selector) throws JMSException {
        try {
            while (!exit) {
                CoreMessage message = receiveCoreMessage(destination, selector);
                log.trace("VNFM-DUMMY: received " + message);
                this.onAction(message);
            }
        }catch(Exception e){
            log.warn("Exiting closing resources...");
        }
    }

    private CoreMessage receiveCoreMessage(String destination, String selector) throws JMSException {
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setPubSubNoLocal(true);
        CoreMessage message = (CoreMessage) ((ObjectMessage) jmsTemplate.receiveSelected(destination, "type = \'" + selector + "\'")).getObject();
        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setPubSubNoLocal(false);
        return message;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

//    protected void sendError(Exception e) throws JMSException, NamingException {
//        CoreMessage coreMessage = new CoreMessage();
//        coreMessage.setAction(Action.ERROR);
//        coreMessage.setPayload(e);
//        this.sendMessageToQueue("org.project.openbaton.common.vnfm-core-actions", coreMessage);
//    }

    protected boolean sendAndReceiveMessage(String receiveFromQueueName, String sendToQueueName, final Serializable vduMessage) throws JMSException {
        sendMessageToQueue(sendToQueueName, vduMessage);
        ObjectMessage objectMessage = (ObjectMessage) jmsTemplate.receive(receiveFromQueueName);
        log.debug("Received: " + objectMessage.getObject());
        VDUMessage answer = (VDUMessage) objectMessage.getObject();
        if (answer.getLifecycleEvent().ordinal() != Event.ERROR.ordinal()){
            return true;
        }
        return false;
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

        jmsTemplate.send(sendToQueueName, messageCreator);
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


        onMessage("core-vnfm-actions", this.SELECTOR);

    }
}
