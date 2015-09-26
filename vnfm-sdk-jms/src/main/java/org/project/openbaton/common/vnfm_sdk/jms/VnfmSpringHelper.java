package org.project.openbaton.common.vnfm_sdk.jms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.common.Ip;
import org.project.openbaton.catalogue.mano.common.LifecycleEvent;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.record.VNFCInstance;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.project.openbaton.catalogue.nfvo.Script;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmErrorMessage;
import org.project.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.project.openbaton.common.vnfm_sdk.VnfmHelper;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.project.openbaton.common.vnfm_sdk.utils.VnfmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by lto on 23/09/15.
 */
@Service
@Scope
public class VnfmSpringHelper extends VnfmHelper {

    private static final String nfvoQueue = "vnfm-core-actions";
    private Gson parser = new GsonBuilder().setPrettyPrinting().create();
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    @Async
    public Future<VirtualNetworkFunctionRecord> grantLifecycleOperation(VirtualNetworkFunctionRecord vnfr) throws VnfmSdkException {
        NFVMessage response;
        try {
            response = sendAndReceiveNfvMessage(nfvoQueue, VnfmUtils.getNfvMessage(Action.GRANT_OPERATION, vnfr));
        } catch (JMSException e) {
            throw new VnfmSdkException("Not able to grant operation", e);
        }
        log.debug("" + response);
        if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
            throw new VnfmSdkException("Not able to grant operation because: " + ((OrVnfmErrorMessage) response).getMessage() , ((OrVnfmErrorMessage) response).getVnfr());
        }
        OrVnfmGenericMessage orVnfmGenericMessage = (OrVnfmGenericMessage) response;
        return new AsyncResult<>(orVnfmGenericMessage.getVnfr());
    }

    @Override
    @Async
    public Future<VirtualNetworkFunctionRecord> allocateResources(VirtualNetworkFunctionRecord vnfr) throws VnfmSdkException {
        NFVMessage response;
        try {
            response = sendAndReceiveNfvMessage(nfvoQueue, VnfmUtils.getNfvMessage(Action.ALLOCATE_RESOURCES, vnfr));
        } catch (JMSException e) {
            log.error("" + e.getMessage());
            throw new VnfmSdkException("Not able to allocate Resources", e);
        }
        if (response.getAction().ordinal() == Action.ERROR.ordinal()) {
            OrVnfmErrorMessage errorMessage = (OrVnfmErrorMessage) response;
            log.error(errorMessage.getMessage());
            throw new VnfmSdkException("Not able to allocate Resources because: " + errorMessage.getMessage() , errorMessage.getVnfr());
        }
        OrVnfmGenericMessage orVnfmGenericMessage = (OrVnfmGenericMessage) response;
        log.debug("Received from ALLOCATE: " + orVnfmGenericMessage.getVnfr());
        return new AsyncResult<>(orVnfmGenericMessage.getVnfr());
    }

    private NFVMessage sendAndReceiveNfvMessage(String destination, NFVMessage nfvMessage) throws JMSException {
        Message response = jmsTemplate.sendAndReceive(destination, getObjectMessageCreator(nfvMessage));
        return (NFVMessage) ((ObjectMessage) response).getObject();
    }

    @Override
    public void sendMessageToQueue(String sendToQueueName, final Serializable message) {
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
     * @param queueName
     * @return
     * @throws JMSException
     */
    protected String receiveTextFromQueue(String queueName) throws JMSException {
        return ((TextMessage) this.jmsTemplate.receive(queueName)).getText();
    }

    @Override
    public void sendToNfvo(final NFVMessage nfvMessage) {
        sendMessageToQueue(nfvoQueue, nfvMessage);
    }

    private String executeActionOnEMS(String vduHostname, String command) throws Exception {
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

    @Override
    public Iterable<String> executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event, Map<String, String> env) throws Exception {//TODO make it parallel
        LinkedList<String> res = new LinkedList<>();
        LifecycleEvent le = VnfmUtils.getLifecycleEvent(virtualNetworkFunctionRecord.getLifecycle_event(), event);
        log.trace("The number of scripts for " + virtualNetworkFunctionRecord.getName() + " are: " + le.getLifecycle_events());

        if (le != null) {
            for (String script : le.getLifecycle_events()) {
                log.info("Sending script: " + script + " to VirtualNetworkFunctionRecord: " + virtualNetworkFunctionRecord.getName());
                for (VirtualDeploymentUnit vdu : virtualNetworkFunctionRecord.getVdu()) {
                    for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                        Map<String, String> tempEnv = new HashMap<>();
                        for (Ip ip : vnfcInstance.getIps()) {
                            log.debug("Adding net: " + ip.getNetName() + " with value: " + ip.getIp());
                            tempEnv.put(ip.getNetName(), ip.getIp());
                        }
                        int i = 1;
                        for (String fip : vnfcInstance.getFloatingIps()) {
                            log.debug("adding floatingIp: " + fip);
                            tempEnv.put("fip" + i, fip);
                            i++;
                        }
                        env.putAll(tempEnv);
                        log.info("Environment Variables are: " + env);
                        String command = getJsonObject("EXECUTE", script, env).toString();
                        res.add(executeActionOnEMS(vnfcInstance.getHostname(), command));
                        for (String key : tempEnv.keySet()) {
                            env.remove(key);
                        }
                    }
                }
            }
            return res;
        }
        throw new VnfmSdkException("Error executing script");
    }

    @Override
    public String executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event, VNFRecordDependency dependency) throws Exception {
        LifecycleEvent le = VnfmUtils.getLifecycleEvent(virtualNetworkFunctionRecord.getLifecycle_event(), event);
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
    public void saveScriptOnEms(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Object scripts) throws Exception {

        log.debug("Scripts are: " + scripts.getClass().getName());

        if (scripts instanceof String) {
            String scriptLink = (String) scripts;
            log.debug("Scripts are: " + scriptLink);
            JsonObject jsonMessage = getJsonObject("CLONE_SCRIPTS", scriptLink);

            for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                    executeActionOnEMS(vnfcInstance.getHostname(), jsonMessage.toString());
                }
            }
        } else if (scripts instanceof Set) {
            Set<Script> scriptSet = (Set<Script>) scripts;

            for (Script script : scriptSet) {
                log.debug("Sending script encoded base64 ");
                String base64String = Base64.encodeBase64String(script.getPayload());
                log.trace("The base64 string is: " + base64String);
                JsonObject jsonMessage = getJsonObjectForScript("SAVE_SCRIPTS", base64String, script.getName());
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                        executeActionOnEMS(vnfcInstance.getHostname(), jsonMessage.toString());
                    }
                }
            }

        }
    }

    @Override
    public NFVMessage sendAndReceive(Action action, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws JMSException {
        return sendAndReceiveNfvMessage(nfvoQueue, VnfmUtils.getNfvMessage(action, virtualNetworkFunctionRecord));
    }

    private JsonObject getJsonObjectForScript(String save_scripts, String payload, String name) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("action", save_scripts);
        jsonMessage.addProperty("payload", payload);
        jsonMessage.addProperty("name", name);
        return jsonMessage;
    }
}
