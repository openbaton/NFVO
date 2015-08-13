package org.project.openbaton.common.vnfm_sdk.jms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.io.Serializable;

/**
 * Created by lto on 28/05/15.
 */

@SpringBootApplication
public abstract class AbstractVnfmSpringJMS extends AbstractVnfm implements MessageListener, JmsListenerConfigurer {

    private Gson parser=new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private JmsTemplate jmsTemplate;


    @Autowired
    private JmsListenerContainerFactory<?> jmsListenerContainerFactory;

    @Bean
    ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory();
    }

    @Bean
    JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("15");
        return factory;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        registrar.setContainerFactory(jmsListenerContainerFactory);
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setDestination("core-" + this.type + "-actions");
        endpoint.setMessageListener(this);
        endpoint.setConcurrency("15");
        endpoint.setId(String.valueOf(Thread.currentThread().getId()));
        registrar.registerEndpoint(endpoint);
    }

    @Override
    public void onMessage(Message message) {
        CoreMessage msg = null;
        try {
            msg = (CoreMessage) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
        log.trace("VNFM: received " + msg);
        this.onAction(msg);
    }


    protected void sendMessageToQueue(String sendToQueueName, final Serializable message) {
        log.debug("Sending message: " + message + " to Queue: " + sendToQueueName);

        MessageCreator messageCreator;

        if (message instanceof java.lang.String )
            messageCreator =getTextMessageCreator((String) message);
        else
            messageCreator = getObjectMessageCreator(message);

        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setPubSubNoLocal(false);
        jmsTemplate.send(sendToQueueName, messageCreator);
    }

    private MessageCreator getTextMessageCreator(final String string) {
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage objectMessage = session.createTextMessage(string);
                return objectMessage;
            }
        };
        return messageCreator;
    }

    private MessageCreator getObjectMessageCreator(final Serializable message) {
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage(message);
                return objectMessage;
            }
        };
        return messageCreator;
    }

    /**
     * This method should be used for receiving text message from EMS
     *
     * resp = {
     *      'output': out,          // the output of the command
     *      'err': err,             // the error outputs of the commands
     *      'status': status        // the exit status of the command
     * }
     *
     * @param queueName
     * @return
     * @throws JMSException
     */
    protected String receiveTextFromQueue(String queueName) throws JMSException {
        return ((TextMessage)this.jmsTemplate.receive(queueName)).getText();
    }

    @Override
    protected void executeActionOnEMS(String vduHostname, String command) throws JMSException, VnfmSdkException {
        this.sendMessageToQueue("vnfm-" + vduHostname + "-actions", command);

        String response = receiveTextFromQueue(vduHostname + "-vnfm-actions");

        log.debug("Received from EMS (" + vduHostname + "): " + response);

        if(response==null) {
            throw new NullPointerException("Response from EMS is null");
        }

        JsonObject jsonObject = parser.fromJson(response,JsonObject.class);

        if(jsonObject.get("status").getAsInt()==0){
            log.debug("Output from EMS ("+vduHostname+") is: " + jsonObject.get("output").getAsString());
        }
        else{
            log.error(jsonObject.get("err").getAsString());
            throw new VnfmSdkException("EMS ("+vduHostname+") had the following error: "+jsonObject.get("err").getAsString());
        }
    }

    @Override
    protected void register() {
        log.debug("Registering to queue: vnfm-register");
        sendMessageToQueue("vnfm-register", vnfmManagerEndpoint);
    }

    @Override
    protected void unregister() {
        this.sendMessageToQueue("vnfm-unregister", vnfmManagerEndpoint);
    }

    @Override
    protected void sendToNfvo(final CoreMessage coreMessage) {
        sendMessageToQueue(nfvoQueue,coreMessage);
    }
}

