package org.openbaton.nfvo.core.api;

import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.createRabbitMqUser;
import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.removeRabbitMqUser;
import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.setRabbitMqUserPermissions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.ServiceMetadata;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.common.configuration.RabbitConfiguration;
import org.openbaton.nfvo.common.utils.key.KeyHelper;
import org.openbaton.nfvo.core.interfaces.VimManagement;
import org.openbaton.nfvo.repositories.ManagerCredentialsRepository;
import org.openbaton.nfvo.repositories.ProjectRepository;
import org.openbaton.nfvo.repositories.ServiceRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.openbaton.nfvo.security.config.OAuth2AuthorizationServerConfig;
import org.openbaton.vnfm.interfaces.register.VnfmRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties
public class ComponentManager implements org.openbaton.nfvo.core.interfaces.ComponentManager {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private OAuth2AuthorizationServerConfig serverConfig;
  @Autowired private Gson gson;
  @Autowired private ServiceRepository serviceRepository;
  @Autowired private ManagerCredentialsRepository managerCredentialsRepository;
  @Autowired private VnfmRegister vnfmRegister;

  @Value("${nfvo.security.service.token.validity:31556952}")
  private int serviceTokenValidityDuration;

  @Value("${spring.rabbitmq.host:localhost}")
  private String brokerIp;

  @Value("${nfvo.rabbit.management.port:15672}")
  private String managementPort;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String rabbitPassword;

  @Value("${spring.rabbitmq.username:admin}")
  private String rabbitUsername;

  @Value("${spring.rabbitmq.virtual-host:/}")
  private String vhost;

  @Value("${nfvo.plugin.refresh.delay:700}")
  private int delayRefresh;

  @Autowired private VnfmEndpointRepository vnfmManagerEndpointRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private VimRepository vimRepository;
  @Autowired private VimManagement vimManagement;

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
  public String registerService(String body)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException, NotFoundException {

    ServiceMetadata service = null;
    String unencryptedBody = null;
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      try {
        unencryptedBody = KeyHelper.decryptNew(body, serviceMetadata.getKeyValue());
      } catch (NoSuchPaddingException
          | NoSuchAlgorithmException
          | InvalidKeyException
          | BadPaddingException
          | IllegalBlockSizeException e) {
        e.printStackTrace();
        continue;
      }

      service = serviceMetadata;
      break;
    }
    if (unencryptedBody == null)
      throw new NotFoundException(
          "Could not decrypt the body, did you enable the service? Is the passed service key correct?");

    JsonObject bodyJson = gson.fromJson(unencryptedBody, JsonObject.class);

    String serviceName = bodyJson.getAsJsonPrimitive("name").getAsString();
    String action = bodyJson.getAsJsonPrimitive("action").getAsString();

    if (!service.getName().equals(serviceName)) {
      log.error("The name of the found service does not match the requested name " + serviceName);
      throw new NotFoundException(
          "The name of the found service does not match the requested name " + serviceName);
    }

    switch (action.toLowerCase()) {
      case "register":
        if (service.getToken() != null && !service.getToken().equals("")) {
          if (service.getTokenExpirationDate() > (new Date()).getTime()) return service.getToken();
        }
        OAuth2AccessToken token = serverConfig.getNewServiceToken(serviceName);
        service.setToken(KeyHelper.encryptNew(token.getValue(), service.getKeyValue()));
        service.setTokenExpirationDate(token.getExpiration().getTime());
        serviceRepository.save(service);
        return service.getToken();

      case "remove":
      case "delete":
        serviceRepository.delete(service);
        log.info("Removed service " + serviceName);
        return null;
      default:
        log.error("Action " + action + " unknown!");
        throw new RuntimeException("Action " + action + " unknown!");
    }
  }

  @Override
  public String createService(String serviceName, String projectId, List<String> projects)
      throws NotFoundException, MissingParameterException {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      if (serviceMetadata.getName().equals(serviceName)) {
        log.debug("Service " + serviceName + " already exists.");
        return serviceMetadata.getKeyValue();
      }
    }
    ServiceMetadata serviceMetadata = new ServiceMetadata();
    serviceMetadata.setRoles(new HashSet<>());

    if (projects.isEmpty()) {
      throw new MissingParameterException("Project list must not be empty");
    }

    if (projects.size() == 1 && projects.get(0).equals("*")) {
      Role r = new Role();
      r.setRole(Role.RoleEnum.ADMIN);
      r.setProject("*");
      serviceMetadata.getRoles().add(r);
    } else {

      for (String project : projects) {
        Role r = new Role();
        r.setRole(Role.RoleEnum.USER);
        Project pr = projectRepository.findFirstById(project);
        pr = pr == null ? projectRepository.findFirstByName(project) : pr;
        if (pr == null) {
          log.error("Project with id or name " + project + " not found");
          throw new NotFoundException("Project with id or name " + project + " not found");
        }
        r.setProject(pr.getName());
        serviceMetadata.getRoles().add(r);
      }
    }

    serviceMetadata.setName(serviceName);
    serviceMetadata.setKeyValue(KeyHelper.genKey());
    log.debug("Saving ServiceMetadata: " + serviceMetadata);
    serviceRepository.save(serviceMetadata);
    return serviceMetadata.getKeyValue();
  }

  @Override
  public boolean isService(String tokenToCheck) {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      if (serviceMetadata.getToken() != null && !serviceMetadata.getToken().equals("")) {
        String encryptedServiceToken = serviceMetadata.getToken();
        String keyData = serviceMetadata.getKeyValue();
        try {
          if (KeyHelper.decryptNew(encryptedServiceToken, keyData).equals(tokenToCheck))
            return true;
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
    serviceRepository.delete(id);
  }

  /*
   * Manager related operations
   */

  /**
   * Handles the registration requests of VNFMs and returns a ManagerCredential object from which
   * the VNFMs can get the rabbitmq username and password.
   *
   * @param message
   * @return
   * @throws IOException
   */
  @Override
  @RabbitListener(
    bindings =
        @QueueBinding(
          value =
              @Queue(
                value = RabbitConfiguration.QUEUE_NAME_MANAGER_REGISTER,
                durable = "true",
                autoDelete = "true"
              ),
          exchange =
              @Exchange(
                value = RabbitConfiguration.EXCHANGE_NAME_OPENBATON,
                ignoreDeclarationExceptions = "true",
                type = RabbitConfiguration.EXCHANGE_TYPE_OPENBATON,
                durable = RabbitConfiguration.EXCHANGE_DURABLE_OPENBATON
              ),
          key = RabbitConfiguration.QUEUE_NAME_MANAGER_REGISTER
        )
  )
  public String enableManager(String message) {
    try {
      // deserialize message
      JsonObject body = gson.fromJson(message, JsonObject.class);
      if (!body.has("action")) {
        log.error("Could not process Json message. The 'action' property is missing.");
        return null;
      }
      JsonElement vnfmManagerEndpoint = body.get("vnfmManagerEndpoint");
      switch (body.get("action").getAsString().toLowerCase()) {
        case "register":
          {

            // register plugin or vnfm
            if (!body.has("type")) {
              log.error("Could not process Json message. The 'type' property is missing.");
              return null;
            }
            String username = body.get("type").getAsString();
            String password = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(16);

            ManagerCredentials managerCredentials =
                managerCredentialsRepository.findFirstByRabbitUsername(username);
            VnfmManagerEndpoint endpoint = null;
            boolean isManager = vnfmManagerEndpoint != null;
            if (managerCredentials != null) {
              log.warn("Manager already registered.");
              return gson.toJson(managerCredentials);
            } else {
              managerCredentials = new ManagerCredentials();
              if (isManager) {
                if (vnfmManagerEndpoint.isJsonPrimitive()) {
                  endpoint =
                      gson.fromJson(vnfmManagerEndpoint.getAsString(), VnfmManagerEndpoint.class);
                } else {
                  endpoint = gson.fromJson(vnfmManagerEndpoint, VnfmManagerEndpoint.class);
                }
              }
            }

            String type =
                isManager
                    ? vnfmManagerEndpoint.getAsJsonObject().get("endpoint").getAsString()
                    : username;
            String configurePermissions =
                "^amq\\.gen.*|amq\\.default$|(" + type + ")|(nfvo." + type + ".actions)";
            String writePermissions =
                "^amq\\.gen.*|amq\\.default$|("
                    + type
                    + ")|(vnfm.nfvo.actions)|(vnfm.nfvo.actions.reply)|(nfvo."
                    + type
                    + ".actions)|(openbaton-exchange)";
            String readPermissions =
                "^amq\\.gen.*|amq\\.default$|(nfvo."
                    + type
                    + ".actions)|("
                    + type
                    + ")|(openbaton-exchange)";

            createRabbitMqUser(
                rabbitUsername,
                rabbitPassword,
                brokerIp,
                managementPort,
                username,
                password,
                vhost);
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
                removeRabbitMqUser(
                    rabbitUsername, rabbitPassword, brokerIp, managementPort, username);
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
            log.info("Registered a new manager.");
            if (!isManager) {
              this.refreshVims(username);
            }
            return gson.toJson(managerCredentials);
          }
        case "unregister":
        case "deregister":
          {
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
                    gson.fromJson(vnfmManagerEndpoint, VnfmManagerEndpoint.class));

              removeRabbitMqUser(
                  rabbitUsername, rabbitPassword, brokerIp, managementPort, username);
            } else {
              log.warn(
                  "Some manager tried to unregister with a wrong password! or maybe i have an inconsistent DB...most probably... ;( ");
            }
            return null;
          }
        default:
          return null;
      }
    } catch (Exception e) {
      log.error("Exception while enabling manager or plugin.");
      e.printStackTrace();
      return null;
    }
  }

  private void refreshVims(String username) {
    if (delayRefresh > 0) {
      Timer timer = new Timer();

      TimerTask action =
          new TimerTask() {
            public void run() {
              if (username.contains(".")) {
                String[] pluginId = username.split(Pattern.quote("."));
                if (pluginId.length == 3 && pluginId[0].equals("vim-drivers")) {
                  String vimType = pluginId[1];
                  log.debug(String.format("Refreshing vims of type %s", vimType));
                  vimRepository
                      .findByType(vimType)
                      .stream()
                      .parallel()
                      .forEach(
                          vim -> {
                            try {
                              vimManagement.refresh(vim, false).get();
                            } catch (VimException
                                | PluginException
                                | IOException
                                | InterruptedException
                                | ExecutionException e) {
                              e.printStackTrace();
                              log.warn(
                                  String.format(
                                      "Error while refreshing vim %s of type %s after plugin registration",
                                      vim.getName(), vim.getType()));
                            }
                          });
                }
              }
            }
          };

      timer.schedule(action, delayRefresh);
    }
  }

  @Override
  public void removeTokens() {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      serviceMetadata.setToken(null);
      serviceRepository.save(serviceMetadata);
    }
  }
}
