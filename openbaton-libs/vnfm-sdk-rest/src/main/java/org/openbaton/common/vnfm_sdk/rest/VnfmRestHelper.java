package org.openbaton.common.vnfm_sdk.rest;

import com.google.gson.Gson;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.openbaton.common.vnfm_sdk.utils.VnfmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * Created by lto on 28/09/15.
 */
@Service
@Scope("prototype")
public class VnfmRestHelper extends VnfmHelper {
    private String server = "localhost";
    private String port = "8080";
    private String url = "http://" + server + ":" + port + "/";
    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Gson mapper;

    public Gson getMapper() {
        return mapper;
    }

    @PostConstruct
    private void init() {
        this.mapper = new Gson();
        this.rest = new RestTemplate();
        this.rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

    }

    @Override
    public void sendMessageToQueue(String sendToQueueName, Serializable message) {
        this.post("admin/v1/vnfm-core-actions", mapper.toJson(message));
    }

    @Override
    public void sendToNfvo(NFVMessage nfvMessage) {
        if (nfvMessage instanceof VnfmOrGenericMessage) {
            VnfmOrGenericMessage message = (VnfmOrGenericMessage) nfvMessage;
            this.post("admin/v1/vnfm-core-actions", mapper.toJson(message, nfvMessage.getClass()));
        }else if (nfvMessage instanceof VnfmOrInstantiateMessage) {
            VnfmOrInstantiateMessage message = (VnfmOrInstantiateMessage) nfvMessage;
            this.post("admin/v1/vnfm-core-actions", mapper.toJson(message, nfvMessage.getClass()));
        }
    }

    @Override
    public NFVMessage sendAndReceive(Action action, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception {
        String path;
        if (action.ordinal() == Action.GRANT_OPERATION.ordinal())
            path = "admin/v1/vnfm-core-grant";
        else
            path = "admin/v1/vnfm-core-allocate";

        return mapper.fromJson(this.post(path, mapper.toJson(VnfmUtils.getNfvMessage(action, virtualNetworkFunctionRecord))), OrVnfmGenericMessage.class);
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

    public void register(VnfmManagerEndpoint body) {
        this.post("admin/v1/vnfm-register", mapper.toJson(body));
    }

    public void unregister(VnfmManagerEndpoint body) {
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

}
