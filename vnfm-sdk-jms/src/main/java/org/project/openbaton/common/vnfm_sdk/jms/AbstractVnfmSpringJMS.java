package org.project.openbaton.common.vnfm_sdk.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.project.openbaton.common.vnfm_sdk.VnfmHelper;
import org.project.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.project.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import javax.jms.*;

/**
 * Created by lto on 28/05/15.
 */

@SpringBootApplication
@ComponentScan(basePackages = "org.project.openbaton")
public abstract class AbstractVnfmSpringJMS extends AbstractVnfm implements MessageListener, JmsListenerConfigurer {

    @Autowired
    private JmsListenerContainerFactory<?> jmsListenerContainerFactory;

    @Autowired
    private ConfigurableApplicationContext context;

    @Bean
    ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory();
    }

    @Bean
    JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
        loadProperties();
        factory.setSessionTransacted(Boolean.valueOf(properties.getProperty("transacted", "false")));
        factory.setConcurrency(properties.getProperty("concurrency", "15"));
        return factory;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        registrar.setContainerFactory(jmsListenerContainerFactory);
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setDestination("core-" + this.type + "-actions");
        endpoint.setMessageListener(this);
        loadProperties();
        endpoint.setConcurrency(properties.getProperty("concurrency", "15"));
        endpoint.setId(String.valueOf(Thread.currentThread().getId()));
        registrar.registerEndpoint(endpoint);
    }

    @Override
    public void onMessage(Message message) {
        NFVMessage msg = null;
        try {
            msg = (NFVMessage) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
        log.trace("VNFM: received " + msg);
        try {
            this.onAction(msg);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (BadFormatException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setup() {
        vnfmHelper = (VnfmHelper) context.getBean("vnfmSpringHelper");
        super.setup();
    }

    protected abstract void checkEmsStarted(String hostname) throws RuntimeException;

    @Override
    protected void unregister() {
        vnfmHelper.sendMessageToQueue("vnfm-unregister", vnfmManagerEndpoint);
    }

    @Override
    protected void register() {
        vnfmHelper.sendMessageToQueue("vnfm-register", vnfmManagerEndpoint);
    }
}

