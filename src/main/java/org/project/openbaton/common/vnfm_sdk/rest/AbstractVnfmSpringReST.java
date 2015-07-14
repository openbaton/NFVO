package org.project.openbaton.common.vnfm_sdk.rest;

import com.google.gson.Gson;
import org.project.openbaton.catalogue.nfvo.CoreMessage;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.Serializable;

/**
 * Created by lto on 08/07/15.
 */
@SpringBootApplication
@RestController
@RequestMapping("/core-vnfm-actions")
public abstract class AbstractVnfmSpringReST extends AbstractVnfm {

    protected final VnfmManagerEndpoint vnfmManagerEndpoint;
    protected String server = "localhost";
    protected String port = "8080";
    private String url = "http://" +server + ":" + port+ "/";
    protected RestTemplate rest;
    protected HttpHeaders headers;
    protected HttpStatus status;
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

    @PreDestroy
    public void shutdown(){
        this.unregister(vnfmManagerEndpoint);
    }

    @Override
    protected void unregister(VnfmManagerEndpoint endpoint){
        this.post("/admin/v1/vnfm_sdk-unregister", mapper.toJson(endpoint));
    }

    public AbstractVnfmSpringReST() {
        this.mapper = new Gson();
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        loadProperties();
        log.debug("SELECTOR: " + this.getEndpoint());

        vnfmManagerEndpoint = new VnfmManagerEndpoint();
        vnfmManagerEndpoint.setType(this.type);
        vnfmManagerEndpoint.setEndpoint(this.endpoint);
        vnfmManagerEndpoint.setEndpointType(EndpointType.REST);

        log.debug("Registering to vnfm_sdk-register");
        register(vnfmManagerEndpoint);
    }

    protected String get(String path) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    protected String post(String path, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
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

    protected void register(VnfmManagerEndpoint endpoint){
        String json = mapper.toJson(endpoint);
        log.debug("post on /admin/v1/vnfm_sdk-register with json: " + json);
        this.post("/admin/v1/vnfm_sdk-register", mapper.toJson(endpoint));
    }

    protected void sendToCore(Serializable msg){
        String json = mapper.toJson(msg);
        log.debug("post on vnfm_sdk-core-actions with json: " + json);
        this.post("/admin/v1/vnfm_sdk-core-actions", json);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //TODO add the validation
    public @ResponseBody String  receive(@RequestBody /*@Valid*/ CoreMessage message){
        log.debug("Received: " + message);
        this.onAction(message);
        return "ack";
    }
}
