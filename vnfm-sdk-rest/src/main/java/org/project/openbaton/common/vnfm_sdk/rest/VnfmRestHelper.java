package org.project.openbaton.common.vnfm_sdk.rest;

import com.google.gson.Gson;
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
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.common.vnfm_sdk.VnfmHelper;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.project.openbaton.common.vnfm_sdk.utils.VnfmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by lto on 28/09/15.
 */
@Service
@Scope("prototype")
public class VnfmRestHelper extends VnfmHelper {
    private String server = "localhost";
    private String port = "8080";
    private String url = "http://" +server + ":" + port+ "/";
    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Gson mapper;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    private void init(){
        this.mapper = new Gson();
        this.rest = new RestTemplate();
        this.rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

    }

    @Override
    public void sendMessageToQueue(String sendToQueueName, Serializable message) {
        this.post("admin/v1/vnfm-core-actions",mapper.toJson(message));
    }

    @Override
    public void sendToNfvo(NFVMessage nfvMessage) {
        this.post("admin/v1/vnfm-core-actions",mapper.toJson(nfvMessage));
    }

    @Override
    public Iterable<String> executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event) throws Exception {
        Map<String, String> env = getMap(virtualNetworkFunctionRecord);
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
                        log.info("Environment Variables are: " + virtualNetworkFunctionRecord);
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
                JsonObject jsonMessage = getJsonObjectForScript("SAVE_SCRIPTS", base64String, script.getName(), scriptPath);
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
                    for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
                        executeActionOnEMS(vnfcInstance.getHostname(), jsonMessage.toString());
                    }
                }
            }

        }
    }

    @Override
    public NFVMessage sendAndReceive(Action action, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception {
        return null;
    }

    private String get(String path) {
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    private String post(String path, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        log.debug("url is: " + url + path);
        log.debug("BODY is: " + json);
        ResponseEntity<String> responseEntity = rest.postForEntity(url + path, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    private void put(String path, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.PUT, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    private void delete(String path) {
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.DELETE, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    public void register(VnfmManagerEndpoint body){
        this.post("admin/v1/vnfm-register", mapper.toJson(body));
    }

    public void unregister(VnfmManagerEndpoint body){
        this.post("admin/v1/vnfm-unregister", mapper.toJson(body));
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public RestTemplate getRest() {
        return rest;
    }

    public void setRest(RestTemplate rest) {
        this.rest = rest;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    private String receiveTextFromQueue(String queueName) throws JMSException {
        return ((TextMessage) this.jmsTemplate.receive(queueName)).getText();
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

        JsonObject jsonObject = mapper.fromJson(response, JsonObject.class);

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
}
