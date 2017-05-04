package org.openbaton.nfvo.security.components;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.openbaton.catalogue.nfvo.ServiceCredentials;
import org.openbaton.catalogue.nfvo.ServiceMetadata;
import org.openbaton.nfvo.repositories.ManaggerCredentialsRepository;
import org.openbaton.nfvo.repositories.ServiceRepository;
import org.openbaton.utils.key.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

//import java.util.Base64;

/** Created by lto on 04/04/2017. */
@Service
@ConfigurationProperties
public class ComponentManager implements org.openbaton.nfvo.security.interfaces.ComponentManager {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private TokenStore tokenStore;
  @Autowired private Gson gson;
  @Autowired private ServiceRepository serviceRepository;
  @Autowired private ManaggerCredentialsRepository managerCredentialsRepository;

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

  @Override
  public ServiceCredentials registerService(String body) {

    ServiceMetadata service = null;
    String unencryptedBody = null;
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      try {
        unencryptedBody =
            KeyHelper.decrypt(body, KeyHelper.restoreKey(serviceMetadata.getKeyValue()));
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
        return null;
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
      } catch (InvalidKeyException e) {
        e.printStackTrace();
        return null;
      } catch (BadPaddingException e) {
        e.printStackTrace();
        return null;
      } catch (IllegalBlockSizeException e) {
        e.printStackTrace();
        return null;
      }

      service = serviceMetadata;
      break;
    }

    if (service == null) {
      log.error("Please enable first your Service in order to get the super duper secret key");
      return null;
    }

    JsonObject bodyJson = gson.fromJson(unencryptedBody, JsonObject.class);

    String serviceName = bodyJson.getAsJsonPrimitive("name").getAsString();
    String action = bodyJson.getAsJsonPrimitive("action").getAsString();

    if (!service.getName().equals(serviceName)) {
      log.error("The Name does not match!");
      return null;
    }

    if (action.toLowerCase().equals("register")) {

      if (service.getStatus().toLowerCase().equals("active")) {
        log.error("Service is already ACTIVE!");
        return null;
      }

      ServiceCredentials serviceCredentials = new ServiceCredentials();

      String token = getNewToken(serviceName);

      try {
        serviceCredentials.setToken(
            KeyHelper.encryptToString(token, KeyHelper.restoreKey(service.getKeyValue())));
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      } catch (BadPaddingException e) {
        e.printStackTrace();
      } catch (IllegalBlockSizeException e) {
        e.printStackTrace();
      }
      service.setStatus("active");
      serviceRepository.save(service);
      return serviceCredentials;
    } else if (action.toLowerCase().equals("unregister")
        || action.toLowerCase().equals("deregister")) {
      service.setStatus("down");
      serviceRepository.save(service);
      log.info("Set status of service " + serviceName + " to down");
      return null;
    } else if (action.toLowerCase().equals("remove") || action.toLowerCase().equals("delete")) {
      serviceRepository.delete(service);
      log.info("Removed service " + serviceName);
      return null;
    } else {
      log.error("Action " + action + " unknown!");
      return null;
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
    serviceMetadata.setStatus("down");
    serviceMetadata.setKeyValue(KeyHelper.genKey().getEncoded());
    log.debug("Saving ServiceMetadata: " + serviceMetadata);
    serviceRepository.save(serviceMetadata);
    return serviceMetadata.getKeyValue();
  }

  private String getNewToken(String serviceName) {
    Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
    authorities.add(new SimpleGrantedAuthority("ADMIN"));

    Map<String, String> requestParameters = new HashMap<>();
    boolean approved = true;
    Set<String> scope = new HashSet<>();
    scope.add("scope");
    Set<String> resourceIds = new HashSet<>();
    Set<String> responseTypes = new HashSet<>();
    responseTypes.add("code");
    Map<String, Serializable> extensionProperties = new HashMap<>();

    OAuth2Request oAuth2Request =
        new OAuth2Request(
            requestParameters,
            serviceName,
            authorities,
            true,
            scope,
            resourceIds,
            null,
            responseTypes,
            extensionProperties);

    User userPrincipal =
        new User(serviceName, "" + Math.random() * 1000, true, true, true, true, authorities);

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

    DefaultTokenServices tokenServices = new DefaultTokenServices();
    tokenServices.setSupportRefreshToken(true);
    tokenServices.setTokenStore(this.tokenStore);
    tokenServices.setAccessTokenValiditySeconds(serviceTokenValidityDuration);

    OAuth2AccessToken token = tokenServices.createAccessToken(auth);
    return token.getValue();
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
      headers.setAccept(new ArrayList<MediaType>() {
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
      org.apache.http.HttpEntity requestEntity = new StringEntity(pass, ContentType.APPLICATION_JSON);

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

      uri = "http://" + brokerIp + ":" + managementPort + "/api/permissions/" + vhost + "/" + username;
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
    } else if (body.get("action").getAsString().toLowerCase().equals("unregister") ||
               body.get("action").getAsString().toLowerCase().equals("deregister")) {

      ManagerCredentials
          managerCredentials =
          managerCredentialsRepository.findFirstByRabbitUsername(body.get("username").getAsString());
      if (body.get("password").getAsString().equals(managerCredentials.getRabbitPassword())) {
        managerCredentialsRepository.delete(managerCredentials);
      }
      return null;
    } else return null;
  }
}
