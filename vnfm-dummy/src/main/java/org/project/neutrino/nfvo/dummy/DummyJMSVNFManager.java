package org.project.neutrino.nfvo.dummy;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.neutrino.nfvo.catalogue.mano.common.Event;
import org.project.neutrino.nfvo.catalogue.mano.common.LifecycleEvent;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.Action;
import org.project.neutrino.nfvo.catalogue.nfvo.CoreMessage;
import org.project.neutrino.nfvo.catalogue.nfvo.VDUMessage;
import org.project.neutrino.nfvo.common.vnfm.AbstractVnfmJMS;
import org.project.neutrino.nfvo.common.vnfm.utils.UtilsJMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.Serializable;

/**
 * Created by lto on 27/05/15.
 */
@SpringBootApplication
@EnableJms
public class DummyJMSVNFManager extends AbstractVnfmJMS {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory();
    }

    private final static String SELECTOR = "dummy-endpoint";

    @Override
    public void instantiate(VirtualNetworkFunctionRecord vnfr) {
        log.info("Instantiation of VirtualNetworkFunctionRecord " + vnfr.getName());
        log.trace("Instantiation of VirtualNetworkFunctionRecord " + vnfr);
        try {
            Thread.sleep((long) (Math.random() * 10000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.debug("Number of events: " + vnfr.getLifecycle_event().size());

        for (LifecycleEvent event : vnfr.getLifecycle_event()){

                try {
                if (event.getEvent().ordinal() == Event.ALLOCATE.ordinal()){
                    CoreMessage coreMessage = new CoreMessage();
                    coreMessage.setAction(Action.ALLOCATE_RESOURCES);

                    vnfr.getLifecycle_event().remove(event);
                    coreMessage.setPayload(vnfr);
                    UtilsJMS.sendToQueue(coreMessage, "vnfm-core-actions");

                    break;
                }else {

                    VDUMessage vduMessage = new VDUMessage();
                    vduMessage.setLifecycleEvent(event.getEvent());
                    vduMessage.setPayload(event.getLifecycle_events().toArray());

                    if (!sendAndReceiveMessage("vnfm-vm-actions", vduMessage))
                        sendError();

                }

            } catch (JMSException e) {
                try {
                    sendError();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                } catch (NamingException e1) {
                    e1.printStackTrace();
                }
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }

        log.debug("I'm out of here");

        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(Action.INSTATIATE_FINISH);


        try {
            UtilsJMS.sendToQueue(coreMessage, "vnfm-core-actions");
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void sendError() throws JMSException, NamingException {
        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(Action.ERROR);
        coreMessage.setPayload("some payload");
        UtilsJMS.sendToQueue(coreMessage, "vnfm-core-actions");
    }

    private boolean sendAndReceiveMessage(String queueName, final Serializable vduMessage) throws JMSException {
        log.debug("Sending message: " + vduMessage + " to Queue: " + queueName);
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage(vduMessage);
                return objectMessage;
            }
        };

        jmsTemplate.send(queueName, messageCreator);
        ObjectMessage objectMessage = (ObjectMessage) jmsTemplate.receive("vm-vnfm-actions");
        log.debug("I've received: " + objectMessage.getObject());
        VDUMessage answer = (VDUMessage) objectMessage.getObject();
        if (answer.getLifecycleEvent().ordinal() != Event.ERROR.ordinal()){
            return true;
        }
        return false;
    }

    @Override
    public void query() {

    }

    @Override
    public void scale() {

    }

    @Override
    public void checkInstantiationFeasibility() {

    }

    @Override
    public void heal() {

    }

    @Override
    public void updateSoftware() {

    }

    @Override
    public void modify() {

    }

    @Override
    public void upgradeSoftware() {

    }

    @Override
    public void terminate() {

    }

    @JmsListener(destination = "core-vnfm-actions", selector = "type = \'" + SELECTOR + "\'", containerFactory = "myJmsContainerFactory")
    public void onMessage(CoreMessage message) throws JMSException {
        log.trace(message.toString());
        this.onAction(message);
    }

    @Bean
    JmsListenerContainerFactory<?> myJmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("10");
        return factory;
    }

    public static void main(String[] args) {
        System.out.println("type=\"" + SELECTOR + "\"");
        ConfigurableApplicationContext context = SpringApplication.run(DummyJMSVNFManager.class, args);
    }
}
