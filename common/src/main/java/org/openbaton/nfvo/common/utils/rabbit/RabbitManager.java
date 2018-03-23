/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
 *
 */

package org.openbaton.nfvo.common.utils.rabbit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.openbaton.exceptions.WrongStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope
public class RabbitManager {

  private static Logger log = LoggerFactory.getLogger(RabbitManager.class);

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static List<String> getQueues(
      String brokerIp, String username, String password, String virtualHost, int managementPort)
      throws IOException {
    List<String> result = new ArrayList<>();
    String encoding = Base64.encodeBase64String((username + ":" + password).getBytes());
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet httpGet =
          new HttpGet(
              "http://"
                  + brokerIp
                  + ":"
                  + managementPort
                  + "/api/queues/"
                  + virtualHost.replace("/", "%2f"));
      httpGet.setHeader("Authorization", "Basic " + encoding);
      log.trace("executing request " + httpGet.getRequestLine());
      CloseableHttpResponse response = httpClient.execute(httpGet);
      HttpEntity entity = response.getEntity();
      JsonArray array;
      try (InputStreamReader inputStreamReader = new InputStreamReader(entity.getContent())) {
        array = gson.fromJson(inputStreamReader, JsonArray.class);
        if (array != null)
          for (JsonElement queueJson : array) {
            String name = queueJson.getAsJsonObject().get("name").getAsString();
            result.add(name);
            log.trace("found queue: " + name);
          }
      }
    }
    //TODO check for errors
    log.trace("found queues: " + result.toString());
    return result;
  }

  public static void createRabbitMqUser(
      String rabbitUsername,
      String rabbitPassword,
      String brokerIp,
      String managementPort,
      String newUserName,
      String newUserPwd,
      String vHost)
      throws IOException, WrongStatusException {
    String uri = "http://" + brokerIp + ":" + managementPort + "/api/users/" + newUserName;
    Gson gson = new Gson();

    //curl -u admin:openbaton -X PUT http://10.147.66.131:15672/api/users/name -d '{"password":"password", "tags":"administrator", "vhost":"openbaton"}' -H "Content-Type: application/json" -H "Accept:application/json"

    HashMap<String, String> map = new HashMap<>();

    map.put("password", newUserPwd);
    map.put("tags", "administrator");
    map.put("vhost", vHost);
    String pass = gson.toJson(map);
    log.trace("Body is: " + pass);
    org.apache.http.HttpEntity requestEntity = new StringEntity(pass, ContentType.APPLICATION_JSON);
    HttpPut put = new HttpPut(uri);
    String authStr = rabbitUsername + ":" + rabbitPassword;
    String encoding = Base64.encodeBase64String(authStr.getBytes());
    put.setHeader("Authorization", "Basic " + encoding);
    put.setHeader(new BasicHeader("Accept", "application/json"));
    put.setHeader(new BasicHeader("Content-type", "application/json"));
    put.setEntity(requestEntity);

    log.trace("Executing request: " + put.getMethod() + " on " + uri);
    // TODO switch to SSL if possible
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      CloseableHttpResponse response = httpClient.execute(put);
      log.trace(String.valueOf("Status: " + response.getStatusLine().getStatusCode()));
      log.trace("Received status: " + response.getStatusLine().getStatusCode());
      if (response.getStatusLine().getStatusCode() != 204 // already exists
          && response.getStatusLine().getStatusCode() != 201) { // create new one
        throw new WrongStatusException(
            "Error creating RabbitMQ user " + newUserName + ": " + response.getStatusLine());
      }
    }
    //curl -u admin:openbaton -X PUT http://10.147.66.131:15672/api/permissions/openbaton/name -d '{"configure":"
    // (^name)", "write":"(^openbaton)|(^name)", "read":"(^name)"}' -H "Content-Type: application/json" -H
    // "Accept:application/json"

  }

  public static void setRabbitMqUserPermissions(
      String rabbitUsername,
      String rabbitPassword,
      String brokerIp,
      String managementPort,
      String username,
      String vHost,
      String configurePermission,
      String writePermission,
      String readPermission)
      throws IOException, WrongStatusException {
    Gson gson = new Gson();
    if (configurePermission == null) configurePermission = "";
    if (writePermission == null) writePermission = "";
    if (readPermission == null) readPermission = "";
    String uri =
        "http://"
            + brokerIp
            + ":"
            + managementPort
            + "/api/permissions/"
            + vHost.replace("/", "%2f")
            + "/"
            + username;
    HttpPut put = new HttpPut(uri);

    HashMap<String, String> map = new HashMap<>();

    map.put("configure", configurePermission);
    map.put("write", writePermission);
    map.put("read", readPermission);
    String stringEntity = gson.toJson(map);

    log.trace("Body is: " + stringEntity);
    String authStr = rabbitUsername + ":" + rabbitPassword;
    String encoding = Base64.encodeBase64String(authStr.getBytes());
    put.setHeader("Authorization", "Basic " + encoding);
    put.setHeader(new BasicHeader("Accept", "application/json"));
    put.setHeader(new BasicHeader("Content-type", "application/json"));
    put.setEntity(new StringEntity(stringEntity, ContentType.APPLICATION_JSON));

    // TODO switch to SSL if possible
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      log.trace("Executing request: " + put.getMethod() + " on " + uri);
      httpClient.execute(put);
      CloseableHttpResponse response = httpClient.execute(put);
      log.trace(String.valueOf("Status: " + response.getStatusLine().getStatusCode()));
      if (response.getStatusLine().getStatusCode() != 204) {
        throw new WrongStatusException(
            "Error setting permissions of RabbitMQ user"
                + username
                + ": "
                + response.getStatusLine());
      }
    }
  }

  /**
   * Removes a user from RabbitMQ. This function does <em>not</em> throw exceptions even if the
   * removal of the user fails.
   *
   * @param rabbitUsername the user who shall execute the remove operation
   * @param rabbitPassword the password of the RabbitMQ instance
   * @param brokerIp the IP where RabbitMQ is running
   * @param managementPort the port used by RabbitMQ
   * @param userToRemove the user to remove
   */
  public static void removeRabbitMqUserQuietly(
      String rabbitUsername,
      String rabbitPassword,
      String brokerIp,
      String managementPort,
      String userToRemove) {
    try {
      String uri = "http://" + brokerIp + ":" + managementPort + "/api/users/" + userToRemove;

      HttpDelete delete = new HttpDelete(uri);
      String authStr = rabbitUsername + ":" + rabbitPassword;
      String encoding = Base64.encodeBase64String(authStr.getBytes());
      delete.setHeader("Authorization", "Basic " + encoding);
      delete.setHeader(new BasicHeader("Accept", "application/json"));
      //        delete.setHeader(new BasicHeader("Content-type", "application/json"));

      log.trace("Executing request: " + delete.getMethod() + " on " + uri);
      // TODO switch to SSL if possible
      try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        CloseableHttpResponse response = httpClient.execute(delete);
        log.trace(String.valueOf("Status: " + response.getStatusLine().getStatusCode()));
        if (response.getStatusLine().getStatusCode() == 404) {
          log.warn("User not found in RabbitMQ. Assuming that it is removed.");
        } else if (response.getStatusLine().getStatusCode() != 204) {
          log.warn(
              "Error removing RabbitMQ user " + userToRemove + ": " + response.getStatusLine());
        }
      }
    } catch (Exception e) {
      log.warn(
          "Ignoring exception while removing RabbitMQ user "
              + userToRemove
              + ": "
              + e.getMessage());
    }
  }
}
