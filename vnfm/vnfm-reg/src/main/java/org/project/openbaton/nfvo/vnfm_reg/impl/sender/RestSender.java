package org.project.openbaton.nfvo.vnfm_reg.impl.sender;

import com.google.gson.Gson;
import org.project.openbaton.common.catalogue.nfvo.CoreMessage;
import org.project.openbaton.common.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 03/06/15.
 */
@Service(value = "restSender")
public class RestSender implements VnfmSender{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected RestTemplate rest;
    protected HttpHeaders headers;
    protected HttpStatus status;
    protected Gson mapper;

    private String get(String path, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    private String post(String path, String json, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.POST, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    private void put(String path, String json, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.PUT, requestEntity,String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    private void delete(String path, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.DELETE, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    protected HttpStatus getStatus() {
        return status;
    }

    protected void setStatus(HttpStatus status) {
        this.status = status;
    }

    @PostConstruct
    private void init(){
        this.mapper = new Gson();
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    @Override
    public void sendCommand(final CoreMessage coreMessage, VnfmManagerEndpoint endpoint) throws JMSException, NamingException {
        this.sendToVnfm(coreMessage, endpoint.getEndpoint());

    }

    public void sendToVnfm(CoreMessage coreMessage, String url) {

        String json = mapper.toJson(coreMessage);
        log.debug("Sending message: " + json + " to url " + url);
        this.post("core-vnfm-actions", json,url);

    }
}
