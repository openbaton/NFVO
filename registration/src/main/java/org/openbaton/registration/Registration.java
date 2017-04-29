package org.openbaton.registration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** This class handles the registration of Vnfms and plugins to the Nfvo. */
@Service
@Scope("prototype")
public class Registration {

  private static Logger log = LoggerFactory.getLogger(Registration.class);
  @Autowired private Gson gson;

  private String username;
  private String password;

  /**
   * This method registers a Vnfm to the Nfvo by sending a request to the nfvo.manager.handling
   * queue using the given RabbitTemplate. The Nfvo's answer contains a username and password which
   * is injected into the RabbitTemplate so that it can send requests to the queue dedicated to the
   * specific Vnfm possessing the RabbitTemplate object.
   *
   * @param rabbitTemplate
   */
  public void registerVnfmToNfvo(RabbitTemplate rabbitTemplate) {

    JsonObject message = new JsonObject();
    message.add("type", new JsonPrimitive("dummy"));
    message.add("action", new JsonPrimitive("register"));
    log.debug("Registering the Vnfm to the Nfvo");
    Object res =
        rabbitTemplate.convertSendAndReceive("nfvo.manager.handling", gson.toJson(message));
    if (res == null)
      throw new IllegalArgumentException("The NFVO's answer to the registration request is null.");
    if (!(res instanceof ManagerCredentials))
      throw new IllegalArgumentException(
          "The NFVO's answer to the registration request should be of type ManagerCredentials, but it is "
              + res.getClass().getSimpleName());
    this.username = ((ManagerCredentials) res).getRabbitUsername();
    this.password = ((ManagerCredentials) res).getRabbitPassword();
    ((CachingConnectionFactory) rabbitTemplate.getConnectionFactory()).setUsername(username);
    ((CachingConnectionFactory) rabbitTemplate.getConnectionFactory()).setPassword(password);
  }

  /**
   * This method deregisters a Vnfm from the Nfvo by sending a request to the nfvo.manager.handling
   * queue using the given RabbitTemplate. The rabbitTemplate object should be obtained from the
   * Vnfm's VnfmSpringHelperRabbit object.
   *
   * @param rabbitTemplate
   */
  public void deregisterVnfmFromNfvo(RabbitTemplate rabbitTemplate) {
    JsonObject message = new JsonObject();
    message.add("username", new JsonPrimitive(this.username));
    message.add("action", new JsonPrimitive("deregister"));
    message.add("password", new JsonPrimitive(this.password));
    log.debug("Deregister the Vnfm from the Nfvo");
    rabbitTemplate.convertSendAndReceive("nfvo.manager.handling", gson.toJson(message));
  }
}
