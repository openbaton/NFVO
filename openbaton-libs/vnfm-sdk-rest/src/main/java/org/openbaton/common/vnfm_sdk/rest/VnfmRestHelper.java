/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.common.vnfm_sdk.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScaledMessage;
import org.openbaton.common.vnfm_sdk.VnfmHelper;
import org.openbaton.common.vnfm_sdk.exception.VnfmSdkException;
import org.openbaton.common.vnfm_sdk.rest.configuration.GsonDeserializerNFVMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;

/**
 * Created by lto on 28/09/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "vnfm.rest")
public class VnfmRestHelper extends VnfmHelper {

  private String nfvoHost;
  private String nfvoPort;
  private String url;
  private String nfvoSsl;
  private RestTemplate rest;
  private HttpHeaders headers;
  private HttpStatus status;
  private Logger log = LoggerFactory.getLogger(this.getClass());
  private Gson mapper;

  @PostConstruct
  private void init() {
    if (nfvoHost == null) {
      log.info("NFVO Ip is not defined. Set to localhost");
      nfvoHost = "localhost";
    }
    if (nfvoPort == null) {
      log.info("NFVO port is not defined. Set to 8080");
      nfvoPort = "8080";
    }

    if (Boolean.parseBoolean(nfvoSsl)) url = "https://" + nfvoHost + ":" + nfvoPort + "/";
    else url = "http://" + nfvoHost + ":" + nfvoPort + "/";

    this.mapper =
        new GsonBuilder()
            .registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage())
            .create();

    if (Boolean.parseBoolean(nfvoSsl))
      this.rest = new RestTemplate(new SslClientHttpRequestFactory());
    else this.rest = new RestTemplate();

    this.rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    this.headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("Accept", "application/json");
  }

  public void sendMessageToQueue(String sendToQueueName, Serializable message) {
    this.post("admin/v1/vnfm-core-actions", mapper.toJson(message));
  }

  @Override
  public void sendToNfvo(NFVMessage nfvMessage) {
    if (nfvMessage instanceof VnfmOrGenericMessage) {
      VnfmOrGenericMessage message = (VnfmOrGenericMessage) nfvMessage;
      this.post("admin/v1/vnfm-core-actions", mapper.toJson(message, nfvMessage.getClass()));
    } else if (nfvMessage instanceof VnfmOrInstantiateMessage) {
      VnfmOrInstantiateMessage message = (VnfmOrInstantiateMessage) nfvMessage;
      this.post("admin/v1/vnfm-core-actions", mapper.toJson(message, nfvMessage.getClass()));
    } else if (nfvMessage instanceof VnfmOrScaledMessage) {
      VnfmOrScaledMessage message = (VnfmOrScaledMessage) nfvMessage;
      this.post("admin/v1/vnfm-core-actions", mapper.toJson(message, nfvMessage.getClass()));
    } else {
      log.warn(
          "Could not send message of type "
              + nfvMessage.getClass().getSimpleName()
              + " to the NFVO");
    }
  }

  @Override
  public NFVMessage sendAndReceive(NFVMessage message) throws Exception {
    String path;
    if (message.getAction().ordinal() == Action.GRANT_OPERATION.ordinal())
      path = "admin/v1/vnfm-core-grant";
    else if (message.getAction().ordinal() == Action.ALLOCATE_RESOURCES.ordinal())
      path = "admin/v1/vnfm-core-allocate";
    else if (message.getAction().ordinal() == Action.SCALING.ordinal())
      path = "admin/v1/vnfm-core-scale";
    else
      throw new VnfmSdkException(
          "Don't know where to send message with action " + message.getAction());

    return mapper.fromJson(this.post(path, mapper.toJson(message)), NFVMessage.class);
  }

  @Override
  public String sendAndReceive(String message, String queueName) throws Exception {
    return this.post("", message);
  }

  private String get(String path) {
    HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
    ResponseEntity<String> responseEntity =
        rest.exchange(url + path, HttpMethod.GET, requestEntity, String.class);
    this.setStatus(responseEntity.getStatusCode());
    return responseEntity.getBody();
  }

  private String post(String path, String json) {
    HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
    log.debug("url is: " + url + path);
    log.debug("BODY is: " + json);
    ResponseEntity<String> responseEntity =
        rest.postForEntity(url + path, requestEntity, String.class);
    this.setStatus(responseEntity.getStatusCode());
    return responseEntity.getBody();
  }

  private void put(String path, String json) {
    HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
    ResponseEntity<String> responseEntity =
        rest.exchange(url + path, HttpMethod.PUT, requestEntity, String.class);
    this.setStatus(responseEntity.getStatusCode());
  }

  private void delete(String path) {
    HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
    ResponseEntity<String> responseEntity =
        rest.exchange(url + path, HttpMethod.DELETE, requestEntity, String.class);
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

  public String getNfvoHost() {
    return nfvoHost;
  }

  public void setNfvoHost(String nfvoHost) {
    this.nfvoHost = nfvoHost;
  }

  public String getNfvoPort() {
    return nfvoPort;
  }

  public void setNfvoPort(String nfvoPort) {
    this.nfvoPort = nfvoPort;
  }

  public String getNfvoSsl() {
    return nfvoSsl;
  }

  public void setNfvoSsl(String nfvoSsl) {
    this.nfvoSsl = nfvoSsl;
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

  public Gson getMapper() {
    return mapper;
  }

  public void setMapper(Gson mapper) {
    this.mapper = mapper;
  }

  /**
   * Necessary in case the NFVO uses SSL.
   */
  class SslClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod)
        throws IOException {

      SSLContext sslContext = getSslContext();

      if (connection instanceof HttpsURLConnection) {
        ((HttpsURLConnection) connection).setHostnameVerifier(new NoopHostnameVerifier());
        ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
      }
      super.prepareConnection(connection, httpMethod);
    }

    private SSLContext getSslContext() {
      try {
        SSLContext sslContext =
            SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        return sslContext;
      } catch (Exception e) {
        log.error("An exception was thrown while retrieving the SSLContext.");
        e.printStackTrace();
        return null;
      }
    }
  }
}
