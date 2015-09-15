package org.project.openbaton.common.vnfm_sdk.jms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.project.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.project.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lto on 28/05/15.
 */

@SpringBootApplication
@ComponentScan(basePackages = "org.project.openbaton")
public abstract class AbstractVnfmSpringJMS extends AbstractVnfm implements MessageListener, JmsListenerConfigurer {

    protected Gson parser = new GsonBuilder().setPrettyPrinting().create();

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
        factory.setSessionTransacted(true);
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


    protected void sendMessageToQueue(String sendToQueueName, final Serializable message) {
        //log.debug("Sending message: " + message + " to Queue: " + sendToQueueName);

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
    protected String executeActionOnEMS(String vduHostname, String command) throws Exception {
        this.sendMessageToQueue("vnfm-" + vduHostname + "-actions", command);

        String response = receiveTextFromQueue(vduHostname + "-vnfm-actions");

        log.debug("Received from EMS (" + vduHostname + "): " + response);

        if(response==null) {
            throw new NullPointerException("Response from EMS is null");
        }

        JsonObject jsonObject = parser.fromJson(response,JsonObject.class);

        if(jsonObject.get("status").getAsInt()==0){
            try {
                log.debug("Output from EMS ("+vduHostname+") is: " + jsonObject.get("output"));
            }catch (Exception e){
                e.printStackTrace();
                throw e;
            }
        }
        else{
            log.error(jsonObject.get("err").getAsString());
            throw new VnfmSdkException("EMS ("+vduHostname+") had the following error: "+jsonObject.get("err").getAsString());
        }
        return response;
    }

    protected Map<String , String> executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event) throws Exception {
        Map<String, String> res = new HashMap<>();
        LifecycleEvent le = getLifecycleEvent(virtualNetworkFunctionRecord.getLifecycle_event_history(), event);
        if (le != null)
        {
            for (String script : le.getLifecycle_events()) {
                String command = getJsonObject("EXECUTE", script).toString();
                log.debug("Sending command: " + command);
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
                    for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                        checkEmsStarted(vnfcInstance.getHostname());
                        res.put(script, executeActionOnEMS(vnfcInstance.getHostname(), command));
                    }
                }
            }
        }
        return res;
    }

    protected abstract void checkEmsStarted(String hostname);

    @Override
    protected void unregister() {
        this.sendMessageToQueue("vnfm-unregister", vnfmManagerEndpoint);
    }

    @Override
    protected void sendToNfvo(final CoreMessage coreMessage) {
        sendMessageToQueue(nfvoQueue,coreMessage);
    }

    @Override
    protected void register() {
        this.sendMessageToQueue("vnfm-register", vnfmManagerEndpoint);
    }

    protected JsonObject getJsonObject(String action, String payload) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("action", action);
        jsonMessage.addProperty("payload", payload);
        return jsonMessage;
    }
}

