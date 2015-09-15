package org.project.openbaton.common.vnfm_sdk.rest;

import com.google.gson.Gson;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.project.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.project.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

//import javax.validation.Valid;
import java.io.Serializable;

/**
 * Created by lto on 08/07/15.
 */
@SpringBootApplication
@RestController
@RequestMapping("/core-vnfm-actions")
public abstract class AbstractVnfmSpringReST extends AbstractVnfm {

    private String server = "localhost";
    private String port = "8080";
    private String url = "http://" +server + ":" + port+ "/";
    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;
    protected Gson mapper;

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

    @Override
    protected void setup() {
        this.mapper = new Gson();
        this.rest = new RestTemplate();
        this.rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");
        super.setup();
    }

    protected String get(String path) {
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    protected String post(String path, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        log.debug("url is: " + url + path);
        log.debug("BODY is: " + json);
        ResponseEntity<String> responseEntity = rest.postForEntity(url + path, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    protected void put(String path, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.PUT, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    protected void delete(String path) {
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

    @Override
    protected void unregister(){
        this.post("admin/v1/vnfm-unregister", mapper.toJson(vnfmManagerEndpoint));
    }

    @Override
    protected void register(){
        String json = mapper.toJson(vnfmManagerEndpoint);
        log.debug("post on /admin/v1/vnfm-register with json: " + json);
        this.post("admin/v1/vnfm-register", json);
    }

    protected void sendToCore(Serializable msg){
        String json = mapper.toJson(msg);
        log.debug("post on vnfm-core-actions with json: " + json);
        this.post("admin/v1/vnfm-core-actions", json);
    }

    @Override
    protected void sendToNfvo(CoreMessage coreMessage) {
        sendToCore(coreMessage);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@RequestBody /*@Valid*/ CoreMessage message) {
        log.debug("Received: " + message);
        try {
            this.onAction(message);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (BadFormatException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
