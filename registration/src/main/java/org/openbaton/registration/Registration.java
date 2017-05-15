package org.openbaton.registration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rabbitmq.client.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeoutException;
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
  public void registerVnfmToNfvo(RabbitTemplate rabbitTemplate, String type) {

    JsonObject message = new JsonObject();
    message.add("type", new JsonPrimitive(type));
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

  /**
   * Sends a registration message to the NFVO and returns a managerCredentials object from which the
   * rabbitmq username and password can be obtained.
   *
   * @param brokerIp
   * @param port
   * @param username
   * @param password
   * @param pluginName
   * @return
   * @throws IOException
   * @throws TimeoutException
   */
  public ManagerCredentials registerPluginToNfvo(
      String brokerIp, int port, String username, String password, String pluginName)
      throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(brokerIp);
    factory.setPort(port);
    factory.setUsername(username);
    factory.setPassword(password);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    // TODO durable?
    channel.exchangeDeclare("openbaton-exchange", "topic", true);

    // TODO handle durable, autodelte and others...
    channel.queueDeclare("nfvo.manager.handling", true, false, true, null);
    channel.queueBind("nfvo.manager.handling", "openbaton-exchange", "");

    channel.basicQos(1);

    channel.queueDeclare(pluginName, false, false, true, null);
    channel.queueBind(pluginName, "openbaton-exchange", pluginName);

    AMQP.BasicProperties properties =
        new AMQP.BasicProperties.Builder().replyTo(pluginName).build();

    String message = "{'type':'" + pluginName + "','action':'register'}";
    log.debug("Sending message: " + message);
    channel.basicPublish(
        "openbaton-exchange", "nfvo.manager.handling", properties, message.getBytes());

    QueueingConsumer consumer = new QueueingConsumer(channel);
    QueueingConsumer.Delivery delivery;
    channel.basicConsume(pluginName, consumer);
    ManagerCredentials managerCredentials = null;
    boolean exit = false;
    while (!exit) {
      try {
        delivery = consumer.nextDelivery();

        byte[] reply = delivery.getBody();
        Object deserialized = deserialize(reply);
        if (!(deserialized instanceof ManagerCredentials))
          throw new RuntimeException(
              "Could not obtain credentials while registering plugin to Nfvo since the reply is no ManagerCredentials object");
        managerCredentials = (ManagerCredentials) deserialized;
        exit = true;
      } catch (Exception e) {
        e.printStackTrace();
        exit = true;
      }
    }
    if (managerCredentials == null)
      throw new RuntimeException("Could not obtain credentials while registering plugin to Nfvo");

    return managerCredentials;
  }

  private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
    return objectInputStream.readObject();
  }
}
