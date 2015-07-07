package org.project.openbaton.nfvo.common.vnfm;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.nfvo.catalogue.mano.common.Event;
import org.project.openbaton.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.catalogue.nfvo.*;
import org.project.openbaton.nfvo.common.vnfm.interfaces.VNFLifecycleManagement;
import org.project.openbaton.nfvo.common.vnfm.utils.UtilsJMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by lto on 28/05/15.
 */

@SpringBootApplication
@EnableJms
public abstract class AbstractVnfmJMS implements CommandLineRunner, VNFLifecycleManagement {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected String type;
    protected String endpoint;
    protected Properties properties;
    protected String SELECTOR;

    @Autowired
    protected JmsListenerContainerFactory topicJmsContainerFactory;

    public String getSELECTOR() {
        return SELECTOR;
    }

    public void setSELECTOR(String SELECTOR) {
        this.SELECTOR = SELECTOR;
    }

    private boolean exit = false;

    @Autowired
    private JmsTemplate jmsTemplate;

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
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setPubSubNoLocal(true);
        while(!exit) {
            CoreMessage message = (CoreMessage) ((ObjectMessage) jmsTemplate.receiveSelected(destination,"type = \'" + selector + "\'")).getObject();
            log.trace("VNFM-DUMMY: received " + message);
            this.onAction(message);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    @Override
    public abstract void instantiate(VirtualNetworkFunctionRecord vnfr);

    @Override
    public abstract void query();

    @Override
    public abstract void scale();

    @Override
    public abstract void checkInstantiationFeasibility();

    @Override
    public abstract void heal();

    @Override
    public abstract void updateSoftware();

    @Override
    public abstract void modify(VirtualNetworkFunctionRecord vnfr);

    @Override
    public abstract void upgradeSoftware();

    @Override
    public abstract void terminate();

    protected void onAction(CoreMessage message) {
        log.trace("VNFM: Received Message: " + message.getAction());
        switch (message.getAction()){
            case INSTANTIATE_FINISH:
                break;
            case ALLOCATE_RESOURCES:
                break;
            case ERROR:
                break;
            case MODIFY:
                this.modify((VirtualNetworkFunctionRecord) message.getPayload());
                break;
            case RELEASE_RESOURCES:
                break;
            case INSTANTIATE:
                this.instantiate((VirtualNetworkFunctionRecord) message.getPayload());
        }
    }

    protected void sendError(Exception e) throws JMSException, NamingException {
        CoreMessage coreMessage = new CoreMessage();
        coreMessage.setAction(Action.ERROR);
        coreMessage.setPayload(e);
        this.sendMessage("vnfm-core-actions", coreMessage);
    }

    protected boolean sendAndReceiveMessage(String receiveFromQueueName, String sendToQueueName, final Serializable vduMessage) throws JMSException {
        sendMessage(sendToQueueName, vduMessage);
        ObjectMessage objectMessage = (ObjectMessage) jmsTemplate.receive(receiveFromQueueName);
        log.debug("Received: " + objectMessage.getObject());
        VDUMessage answer = (VDUMessage) objectMessage.getObject();
        if (answer.getLifecycleEvent().ordinal() != Event.ERROR.ordinal()){
            return true;
        }
        return false;
    }

    protected void sendMessage(String sendToQueueName, final Serializable vduMessage) {
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

        VnfmManagerEndpoint vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        vnfmManagerEndpoint.setEndpointType(EndpointType.JMS);

        log.debug("Registering to queue: vnfm-register");
        UtilsJMS.sendToRegister(vnfmManagerEndpoint);


        onMessage("core-vnfm-actions", this.SELECTOR);

    }

    private void loadProperties() {
            Resource resource = new ClassPathResource("conf.properties");
        properties = new Properties();
        try {
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
        this.endpoint = (String) properties.get("endpoint");
        this.type = (String) properties.get("type");
    }
}
