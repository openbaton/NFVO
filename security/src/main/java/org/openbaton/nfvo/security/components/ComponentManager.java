package org.openbaton.nfvo.security.components;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.openbaton.catalogue.nfvo.ServiceMetadata;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.ManagerCredentialsRepository;
import org.openbaton.nfvo.repositories.ServiceRepository;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.openbaton.nfvo.security.authentication.OAuth2AuthorizationServerConfig;
import org.openbaton.utils.key.KeyHelper;
import org.openbaton.vnfm.interfaces.register.VnfmRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static org.openbaton.utils.rabbit.RabbitManager.createRabbitMqUser;
import static org.openbaton.utils.rabbit.RabbitManager.removeRabbitMqUser;
import static org.openbaton.utils.rabbit.RabbitManager.setRabbitMqUserPermissions;

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
  @Autowired private VnfmRegister vnfmRegister;

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

  @Value("${spring.rabbitmq.virtual-host:/}")
  private String vhost;

  @Autowired private VnfmEndpointRepository vnfmManagerEndpointRepository;

  /*
   * Service related operations
   */

  /**
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
        if (service.getTokenExpirationDate() > (new Date()).getTime()) return service.getToken();
      }
      OAuth2AccessToken token = serverConfig.getNewServiceToken(serviceName);
      service.setToken(
          KeyHelper.encryptToString(token.getValue(), KeyHelper.restoreKey(service.getKeyValue())));
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

  @Override
  public Iterable<ServiceMetadata> listServices() {
    return serviceRepository.findAll();
  }

  @Override
  public void removeService(String id) {
    //TODO remove also associated toker
    ServiceMetadata serviceMetadataToRemove = serviceRepository.findById(id);
    log.debug("Found service: " + serviceMetadataToRemove);
    if (serviceMetadataToRemove.getToken() != null)
        serverConfig.tokenServices().revokeToken(serviceMetadataToRemove.getToken());
    serviceRepository.delete(id);
  }

  /*
   * Manager related operations
   */

  public ManagerCredentials enableManager(byte[] message) {
    return enableManager(new String(message));
  }

  /**
   * Handles the registration requests of VNFMs and returns a ManagerCredential object from which
   * the VNFMs can get the rabbitmq username and password.
   *
   * @param message
   * @return
   * @throws IOException
   */
  @Override
  public ManagerCredentials enableManager(String message) {
    try {
      // deserialize message
      JsonObject body = gson.fromJson(message, JsonObject.class);
      if (!body.has("action")) {
        log.error("Could not process Json message. The 'action' property is missing.");
        return null;
      }
      if (body.get("action").getAsString().toLowerCase().equals("register")) {

        // register plugin or vnfm
        if (!body.has("type")) {
          log.error("Could not process Json message. The 'type' property is missing.");
          return null;
        }
        String username = body.get("type").getAsString();
        String password = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(16);

        ManagerCredentials managerCredentials =
            managerCredentialsRepository.findFirstByRabbitUsername(username);
        VnfmManagerEndpoint endpoint;
        if (managerCredentials != null) {
          log.error("Manager already registered.");
          return managerCredentials;
        } else {
          managerCredentials = new ManagerCredentials();
          endpoint = gson.fromJson(body.get("vnfmManagerEndpoint"), VnfmManagerEndpoint.class);
        }

        //          String regexOpenbaton = "(^nfvo)";
        //          String regexManager = "(^" + username + ")|(openbaton-exchange)";
        //          String regexBoth = regexOpenbaton + "|" + regexManager;

        String configurePermissions = "(" + username + ")|(nfvo." + username + ".actions)";
        String writePermissions =
            "^amq\\.gen.*|amq\\.default$|("
                + username
                + ")|(vnfm.nfvo.actions)|(vnfm.nfvo.actions.reply)|(nfvo."
                + username
                + ".actions)|(openbaton-exchange)";
        String readPermissions =
            "(nfvo." + username + ".actions)|(" + username + ")|(openbaton-exchange)";

        createRabbitMqUser(
            rabbitUsername, rabbitPassword, brokerIp, managementPort, username, password, vhost);
        try {
          setRabbitMqUserPermissions(
              rabbitUsername,
              rabbitPassword,
              brokerIp,
              managementPort,
              username,
              vhost,
              configurePermissions,
              writePermissions,
              readPermissions);
        } catch (Exception e) {
          try {
            removeRabbitMqUser(rabbitUsername, rabbitPassword, brokerIp, managementPort, username);
          } catch (Exception e2) {
            log.error("Clean up failed. Could not remove RabbitMQ user " + username);
            e2.printStackTrace();
          }
          throw e;
        }

        managerCredentials.setRabbitUsername(username);
        managerCredentials.setRabbitPassword(password);
        managerCredentials = managerCredentialsRepository.save(managerCredentials);
        if (endpoint != null) vnfmManagerEndpointRepository.save(endpoint);
        return managerCredentials;
      } else if (body.get("action").getAsString().toLowerCase().equals("unregister")
          || body.get("action").getAsString().toLowerCase().equals("deregister")) {

        if (!body.has("username")) {
          log.error("Could not process Json message. The 'username' property is missing.");
          return null;
        }
        if (!body.has("password")) {
          log.error("Could not process Json message. The 'password' property is missing.");
          return null;
        }
        String username = body.get("username").getAsString();
        ManagerCredentials managerCredentials =
            managerCredentialsRepository.findFirstByRabbitUsername(username);
        if (managerCredentials == null) {
          log.error("Did not find manager with name " + body.get("username"));
          return null;
        }
        if (body.get("password").getAsString().equals(managerCredentials.getRabbitPassword())) {
          managerCredentialsRepository.delete(managerCredentials);
          // if message comes from a vnfm, remove the endpoint
          if (body.has("vnfmManagerEndpoint"))
            vnfmRegister.unregister(
                gson.fromJson(body.get("vnfmManagerEndpoint"), VnfmManagerEndpoint.class));

          removeRabbitMqUser(rabbitUsername, rabbitPassword, brokerIp, managementPort, username);
        }
        return null;
      } else return null;
    } catch (Exception e) {
      log.error("Exception while enabling manager or plugin.");
      e.printStackTrace();
      return null;
    }
  }
}
