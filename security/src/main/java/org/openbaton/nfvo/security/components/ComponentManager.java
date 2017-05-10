package org.openbaton.nfvo.security.components;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.openbaton.catalogue.nfvo.ServiceMetadata;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.ManagerCredentialsRepository;
import org.openbaton.nfvo.repositories.ServiceRepository;
import org.openbaton.nfvo.security.authentication.OAuth2AuthorizationServerConfig;
import org.openbaton.utils.key.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

//import java.util.Base64;

/** Created by lto on 04/04/2017. */
@Service
@ConfigurationProperties
public class ComponentManager implements org.openbaton.nfvo.security.interfaces.ComponentManager {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private OAuth2AuthorizationServerConfig serverConfig;
  //  @Autowired private TokenStore tokenStore;
  //  @Autowired private DefaultTokenServices tokenServices;
  @Autowired private Gson gson;
  @Autowired private ServiceRepository serviceRepository;
  @Autowired private ManagerCredentialsRepository managerCredentialsRepository;

  @Value("${nfvo.security.service.token.validity:31556952}")
  private int serviceTokenValidityDuration;

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String brokerIp;

  @Value("${nfvo.rabbit.managementPort:15672}")
  private String managementPort;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String rabbitPassword;

  @Value("${spring.rabbitmq.username:admin}")
  private String rabbitUsername;

  @Value("${spring.rabbitmq.vhost:openbaton}")
  private String vhost;

  /*
   * Service related operations
   */

    /**
     *
     * @param body
     * @return an encrypted token
     * @throws NotFoundException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
  @Override
  public String registerService(byte[] body)
      throws NotFoundException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException {

    ServiceMetadata service = null;
    String unencryptedBody = null;
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      try {
        unencryptedBody =
            KeyHelper.decrypt(body, KeyHelper.restoreKey(serviceMetadata.getKeyValue()));
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
        continue;
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        continue;
      } catch (InvalidKeyException e) {
        e.printStackTrace();
        continue;
      } catch (BadPaddingException e) {
        e.printStackTrace();
        continue;
      } catch (IllegalBlockSizeException e) {
        e.printStackTrace();
        continue;
      }

      if (unencryptedBody == null)
        throw new NotFoundException("Could not decrypt the body, did you enabled the service?");

      service = serviceMetadata;
      break;
    }

    if (service == null) {
      log.error("Please create your Service first in order to get the super duper secret key");
      throw new NotFoundException(
          "No Service found. Please create your Service before registering it.");
    }

    JsonObject bodyJson = gson.fromJson(unencryptedBody, JsonObject.class);

    String serviceName = bodyJson.getAsJsonPrimitive("name").getAsString();
    String action = bodyJson.getAsJsonPrimitive("action").getAsString();

    if (!service.getName().equals(serviceName)) {
      log.error(
          "The name of the found Service does not match to the requested name " + serviceName);
      throw new NotFoundException(
          "The name of the found Service does not match to the requested name " + serviceName);
    }

    if (action.toLowerCase().equals("register")) {
        if (service.getToken() != null && !service.getToken().equals("")) {
            if (service.getTokenExpirationDate() > (new Date()).getTime())
                return service.getToken();
        }
          OAuth2AccessToken token = serverConfig.getNewServiceToken(serviceName);
        service.setToken(
              KeyHelper.encryptToString(
                  token.getValue(), KeyHelper.restoreKey(service.getKeyValue())));
        service.setTokenExpirationDate(token.getExpiration().getTime());
        serviceRepository.save(service);
        return service.getToken();

    } else if (action.toLowerCase().equals("remove") || action.toLowerCase().equals("delete")) {
      serviceRepository.delete(service);
      log.info("Removed service " + serviceName);
      return null;
    } else {
      log.error("Action " + action + " unknown!");
      throw new RuntimeException("Action " + action + " unknown!");
    }
  }

  @Override
  public byte[] createService(String serviceName, String projectId)
      throws NoSuchAlgorithmException, IOException {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      if (serviceMetadata.getName().equals(serviceName)) {
        log.debug("Service " + serviceName + " already exists.");
        return serviceMetadata.getKeyValue();
      }
    }
    ServiceMetadata serviceMetadata = new ServiceMetadata();
    serviceMetadata.setName(serviceName);
    serviceMetadata.setKeyValue(KeyHelper.genKey().getEncoded());
    log.debug("Saving ServiceMetadata: " + serviceMetadata);
    serviceRepository.save(serviceMetadata);
    return serviceMetadata.getKeyValue();
  }

  @Override
  public boolean isService(String tokenToCheck)
      throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
          IllegalBlockSizeException, NoSuchPaddingException {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
        if (serviceMetadata.getToken() != null && !serviceMetadata.getToken().equals("")) {
            String encryptedServiceToken = serviceMetadata.getToken();
            byte[] keyData = serviceMetadata.getKeyValue();
            Key key = KeyHelper.restoreKey(keyData);
            try {
                if (KeyHelper.decrypt(encryptedServiceToken, key).equals(tokenToCheck)) return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    return false;
  }

  /*
   * Manager related operations
   */

  @Override
  public ManagerCredentials enableManager(String message) throws IOException {
    JsonObject body = gson.fromJson(message, JsonObject.class);
    if (body.get("action").getAsString().toLowerCase().equals("register")) {
      ManagerCredentials managerCredentials = new ManagerCredentials();

      String username = body.get("type").getAsString();
      String password = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(16);
      String uri = "http://" + brokerIp + ":" + managementPort + "/api/users/" + username;

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(
          new ArrayList<MediaType>() {
            {
              add(MediaType.APPLICATION_JSON);
            }
          });

      CloseableHttpClient httpclient = HttpClients.createDefault();

      //curl -u admin:openbaton -X PUT http://10.147.66.131:15672/api/users/name -d '{"password":"password", "tags":"administrator", "vhost":"openbaton"}' -H "Content-Type: application/json" -H "Accept:application/json"

      HashMap<String, String> map = new HashMap<>();

      map.put("password", password);
      map.put("tags", "administrator");
      map.put("vhost", vhost);
      String pass = gson.toJson(map);
      log.debug("Body is: " + pass);
      org.apache.http.HttpEntity requestEntity =
          new StringEntity(pass, ContentType.APPLICATION_JSON);

      HttpPut put = new HttpPut(uri);
      String authStr = rabbitUsername + ":" + rabbitPassword;
      String encoding = Base64.encodeBase64String(authStr.getBytes());
      put.setHeader("Authorization", "Basic " + encoding);
      put.setHeader(new BasicHeader("Accept", MediaType.APPLICATION_JSON_VALUE));
      put.setHeader(new BasicHeader("Content-type", MediaType.APPLICATION_JSON_VALUE));
      put.setEntity(requestEntity);

      log.debug("Executing request: " + put.getMethod() + " on " + uri);

      CloseableHttpResponse response = httpclient.execute(put);
      log.debug(String.valueOf("Status: " + response.getStatusLine().getStatusCode()));
      if (response.getStatusLine().getStatusCode() != 204) {
        log.error("Error creating user: " + response.getStatusLine());
        return null;
      }

      //curl -u admin:openbaton -X PUT http://10.147.66.131:15672/api/permissions/openbaton/name -d '{"configure":"
      // (^name)", "write":"(^openbaton)|(^name)", "read":"(^name)"}' -H "Content-Type: application/json" -H
      // "Accept:application/json"

      uri =
          "http://"
              + brokerIp
              + ":"
              + managementPort
              + "/api/permissions/"
              + vhost
              + "/"
              + username;
      put = new HttpPut(uri);

      String regexOpenbaton = "(^nfvo)";
      String regexManager = "(^" + username + ")";
      String regexBoth = regexOpenbaton + "|" + regexManager;
      map = new HashMap<>();
      map.put("configure", regexManager);
      map.put("write", regexBoth);
      map.put("read", regexManager);
      String stringEntity = gson.toJson(map);

      log.debug("Body is: " + stringEntity);
      put.setHeader("Authorization", "Basic " + encoding);
      put.setHeader(new BasicHeader("Accept", MediaType.APPLICATION_JSON_VALUE));
      put.setHeader(new BasicHeader("Content-type", MediaType.APPLICATION_JSON_VALUE));
      put.setEntity(new StringEntity(stringEntity, ContentType.APPLICATION_JSON));

      log.debug("Executing request: " + put.getMethod() + " on " + uri);
      httpclient.execute(put);
      response = httpclient.execute(put);
      log.debug(String.valueOf("Status: " + response.getStatusLine().getStatusCode()));
      if (response.getStatusLine().getStatusCode() != 204) {
        log.error("Error creating user: " + response.getStatusLine());
        return null;
      }

      managerCredentials.setRabbitUsername(username);
      managerCredentials.setRabbitPassword(password);
      managerCredentialsRepository.save(managerCredentials);
      return managerCredentials;
    } else if (body.get("action").getAsString().toLowerCase().equals("unregister")
        || body.get("action").getAsString().toLowerCase().equals("deregister")) {

      ManagerCredentials managerCredentials =
          managerCredentialsRepository.findFirstByRabbitUsername(
              body.get("username").getAsString());
      if (body.get("password").getAsString().equals(managerCredentials.getRabbitPassword())) {
        managerCredentialsRepository.delete(managerCredentials);
      }
      return null;
    } else return null;
  }
}
