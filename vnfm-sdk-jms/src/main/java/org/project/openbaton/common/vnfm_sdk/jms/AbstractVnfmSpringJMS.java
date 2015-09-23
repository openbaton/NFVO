package org.project.openbaton.common.vnfm_sdk.jms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.jms.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Future;

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
    @Async
    protected Future<VirtualNetworkFunctionRecord> grantLifecycleOperation(VirtualNetworkFunctionRecord vnfr) throws VnfmSdkException {
        NFVMessage response;
        try {
            response = sendAndReceiveNfvMessage(nfvoQueue, getNfvMessage(Action.GRANT_OPERATION, vnfr));
        } catch (JMSException e) {
            throw new VnfmSdkException("Not able to grant operation", e);
        }
        log.debug("" + response);
        if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
            throw new VnfmSdkException("Not able to grant operation");
        }
        OrVnfmGenericMessage orVnfmGenericMessage = (OrVnfmGenericMessage) response;
        return new AsyncResult<>(orVnfmGenericMessage.getVnfr());
    }

    @Override
    @Async
    protected Future<VirtualNetworkFunctionRecord> allocateResources(VirtualNetworkFunctionRecord vnfr) throws VnfmSdkException {
        NFVMessage response;
        try {
            response = sendAndReceiveNfvMessage(nfvoQueue, getNfvMessage(Action.ALLOCATE_RESOURCES, vnfr));
        } catch (JMSException e) {
            log.error("" + e.getMessage());
            throw new VnfmSdkException("Not able to allocate Resources", e);
        }
        if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
            throw new VnfmSdkException("Not able to allocate Resources");
        }
        OrVnfmGenericMessage orVnfmGenericMessage = (OrVnfmGenericMessage) response;
        log.debug("Received from ALLOCATE: " + orVnfmGenericMessage.getVnfr());
        return new AsyncResult<>(orVnfmGenericMessage.getVnfr());
    }

    private NFVMessage sendAndReceiveNfvMessage(String destination, NFVMessage nfvMessage) throws JMSException {
        Message response = jmsTemplate.sendAndReceive(destination, getObjectMessageCreator(nfvMessage));
        return (NFVMessage) ((ObjectMessage) response).getObject();
    }

    protected void sendMessageToQueue(String sendToQueueName, final Serializable message) {
        log.trace("Sending message: " + message + " to Queue: " + sendToQueueName);

        MessageCreator messageCreator;

        if (message instanceof java.lang.String)
            messageCreator = getTextMessageCreator((String) message);
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
     * <p/>
     * resp = {
     * 'output': out,          // the output of the command
     * 'err': err,             // the error outputs of the commands
     * 'status': status        // the exit status of the command
     * }
     *
     * @param vduHostname
     * @return
     * @throws JMSException
     */
    protected String receiveTextFromQueue(String queueName) throws JMSException {
        return ((TextMessage) this.jmsTemplate.receive(queueName)).getText();
    }

    @Override
    protected String executeActionOnEMS(String vduHostname, String command) throws Exception {

        int i = 0;
        while (true) {
            log.debug("Waiting for ems to be started... (" + i * 5 + " secs)");
            i++;
            try {
                checkEmsStarted(vduHostname);
                break;
            } catch (RuntimeException e) {
                if (i == 100) {
                    throw e;
                }
                Thread.sleep(5000);
            }
        }
        log.trace("Sending message: " + command + " to " + vduHostname);
        this.sendMessageToQueue("vnfm-" + vduHostname + "-actions", command);

        log.info("Waiting answer from EMS - " + vduHostname);
        String response = receiveTextFromQueue(vduHostname + "-vnfm-actions");

        log.debug("Received from EMS (" + vduHostname + "): " + response);

        if (response == null) {
            throw new NullPointerException("Response from EMS is null");
        }

        JsonObject jsonObject = parser.fromJson(response, JsonObject.class);

        if (jsonObject.get("status").getAsInt() == 0) {
            try {
                log.debug("Output from EMS (" + vduHostname + ") is: " + jsonObject.get("output"));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            log.error(jsonObject.get("err").getAsString());
            throw new VnfmSdkException("EMS (" + vduHostname + ") had the following error: " + jsonObject.get("err").getAsString());
        }
        return response;
    }

    protected Iterable<String> executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event, Map<String, String> env) throws Exception {//TODO make it parallel
        LinkedList<String> res = new LinkedList<>();
        LifecycleEvent le = getLifecycleEvent(virtualNetworkFunctionRecord.getLifecycle_event(), event);
        log.debug("The number of scripts for " + virtualNetworkFunctionRecord.getName() + " are: " + le.getLifecycle_events());

        if (le != null) {
            for (String script : le.getLifecycle_events()) {
                log.info("Sending script: " + script + " to VirtualNetworkFunctionRecord: " + virtualNetworkFunctionRecord.getName());
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
                    for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                        log.info("Environment Variables are: " + env);
                        String command = getJsonObject("EXECUTE", script, env).toString();
                        res.add(executeActionOnEMS(vnfcInstance.getHostname(), command));
                    }
                }
            }
            return res;
        }
        throw new VnfmSdkException("Error executing script");
    }

    protected String executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event, VNFRecordDependency dependency) throws Exception {
        LifecycleEvent le = getLifecycleEvent(virtualNetworkFunctionRecord.getLifecycle_event(), event);
        if (le != null) {
            for (String script : le.getLifecycle_events()) {

                String type = script.substring(0, script.indexOf("_"));
                log.info("Sending command: " + script + " to adding relation with type: " + type + " from VirtualNetworkFunctionRecord " + virtualNetworkFunctionRecord.getName());

                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
                    for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                        Map<String, String> params = new HashMap<>();
                        params.putAll(dependency.getParameters().get(type).getParameters());

                        for (ConfigurationParameter configurationParameter : virtualNetworkFunctionRecord.getConfigurations().getConfigurationParameters())
                            params.put(configurationParameter.getConfKey(), configurationParameter.getValue());
                        String command = getJsonObject("EXECUTE", script, params).toString();
                        return executeActionOnEMS(vnfcInstance.getHostname(), command);
                    }
                }
            }
        }
        throw new VnfmSdkException("Error executing script");
    }

    protected abstract void checkEmsStarted(String hostname) throws RuntimeException;

    @Override
    protected void unregister() {
        this.sendMessageToQueue("vnfm-unregister", vnfmManagerEndpoint);
    }

    @Override
    protected void sendToNfvo(final NFVMessage nfvMessage) {
        sendMessageToQueue(nfvoQueue, nfvMessage);
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

    private JsonObject getJsonObject(String action, String payload, Map<String, String> dependencyParameters) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("action", action);
        jsonMessage.addProperty("payload", payload);
        jsonMessage.add("env", parser.fromJson(parser.toJson(dependencyParameters), JsonObject.class));
        return jsonMessage;
    }

    @Override
    protected void saveScriptLink(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, String scriptsLink) throws Exception {
        log.debug("Scripts are: " + scriptsLink);
        JsonObject jsonMessage = getJsonObject("SAVE_SCRIPTS", scriptsLink);

        for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
            for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                executeActionOnEMS(vnfcInstance.getHostname(), jsonMessage.toString());
            }
        }
    }
}

