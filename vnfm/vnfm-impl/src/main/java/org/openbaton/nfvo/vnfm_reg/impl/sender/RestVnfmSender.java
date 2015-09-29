/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.vnfm_reg.impl.sender;

import com.google.gson.Gson;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.vnfm.interfaces.sender.VnfmSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.jms.Destination;

/**
 * Created by lto on 03/06/15.
 */
@Service
@Scope
public class RestVnfmSender implements VnfmSender {

    protected RestTemplate rest;
    protected HttpHeaders headers;
    protected HttpStatus status;
    protected Gson mapper;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String get(String path, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    private String post(String json, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.POST, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    private void put(String path, String json, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(url + path, HttpMethod.PUT, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
    }

    private void delete(String path, String url) {
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
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
    private void init() {
        this.mapper = new Gson();
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    @Override
    public void sendCommand(final NFVMessage nfvMessage, VnfmManagerEndpoint endpoint) {
        this.sendToVnfm(nfvMessage, endpoint.getEndpoint());
    }

    @Override
    public void sendCommand(NFVMessage nfvMessage, Destination tempDestination) {
        String json = mapper.toJson(nfvMessage);
        log.debug("Sending message: " + json + " to url " + tempDestination);
        throw new UnsupportedOperationException("not implemented");
    }

    public void sendToVnfm(NFVMessage nfvMessage, String url) {
        String json = mapper.toJson(nfvMessage);
        log.debug("Sending message: " + json + " to url " + url);
        this.post(json, url);
    }
}
