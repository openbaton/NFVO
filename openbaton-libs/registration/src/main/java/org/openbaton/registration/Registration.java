package org.openbaton.registration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rabbitmq.client.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
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
  public String[] registerVnfmToNfvo(RabbitTemplate rabbitTemplate, VnfmManagerEndpoint endpoint) {

    JsonObject message = new JsonObject();
    message.add("type", new JsonPrimitive(endpoint.getType()));
    message.add("action", new JsonPrimitive("register"));
    message.add("vnfmManagerEndpoint", gson.toJsonTree(endpoint, VnfmManagerEndpoint.class));
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
    String[] usernamePassword = new String[2];
    usernamePassword[0] = username;
    usernamePassword[1] = password;
    return usernamePassword;
  }

  /**
   * This method deregisters a Vnfm from the Nfvo by sending a request to the nfvo.manager.handling
   * queue using the given RabbitTemplate. The rabbitTemplate object should be obtained from the
   * Vnfm's VnfmSpringHelperRabbit object.
   *
   * @param rabbitTemplate
   */
  public void deregisterVnfmFromNfvo(RabbitTemplate rabbitTemplate, VnfmManagerEndpoint endpoint) {
    JsonObject message = new JsonObject();
    message.add("username", new JsonPrimitive(this.username));
    message.add("action", new JsonPrimitive("deregister"));
    message.add("password", new JsonPrimitive(this.password));
    message.add("vnfmManagerEndpoint", gson.toJsonTree(endpoint));
    log.debug("Deregister the Vnfm from the Nfvo");
    rabbitTemplate.convertAndSend("nfvo.manager.handling", gson.toJson(message));
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
      String brokerIp,
      int port,
      String username,
      String password,
      String virtualHost,
      String pluginName)
      throws IOException, TimeoutException, InterruptedException {
    String message = "{'type':'" + pluginName + "','action':'register'}";
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(brokerIp);
    factory.setPort(port);
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    // check if exchange and queue exist
    channel.exchangeDeclarePassive("openbaton-exchange");
    channel.queueDeclarePassive("nfvo.manager.handling");
    channel.basicQos(1);

    String replyQueueName = "amq.rabbitmq.reply-to";
    final String corrId = UUID.randomUUID().toString();

    AMQP.BasicProperties props =
        new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();

    final BlockingQueue<ManagerCredentials> response =
        new ArrayBlockingQueue<ManagerCredentials>(1);

    channel.basicConsume(
        replyQueueName,
        true,
        new DefaultConsumer(channel) {
          @Override
          public void handleDelivery(
              String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
              throws IOException {
            if (properties.getCorrelationId().equals(corrId)) {
              ManagerCredentials managerCredentials = null;
              ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
              ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
              Object replyObject = null;
              try {
                replyObject = objectInputStream.readObject();
              } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                    "Could not deserialize the registration request's reply.", e.getCause());
              }
              if (!(replyObject instanceof ManagerCredentials))
                throw new RuntimeException(
                    "Could not obtain credentials while registering plugin to Nfvo since the reply is no ManagerCredentials object");
              managerCredentials = (ManagerCredentials) replyObject;
              response.offer(managerCredentials);
            }
          }
        });

    channel.basicPublish(
        "openbaton-exchange", "nfvo.manager.handling", props, message.getBytes("UTF-8"));

    ManagerCredentials managerCredentials = response.take();

    channel.close();
    connection.close();
    return managerCredentials;
  }

  public void deregisterPluginFromNfvo(
      String brokerIp,
      int port,
      String username,
      String password,
      String virtualHost,
      String managerCredentialUsername,
      String managerCredentialPassword)
      throws IOException, TimeoutException {
    String message =
        "{'username':'"
            + managerCredentialUsername
            + "','action':'deregister','password':'"
            + managerCredentialPassword
            + "'}";
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(brokerIp);
    factory.setPort(port);
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    // check if exchange and queue exist
    channel.exchangeDeclarePassive("openbaton-exchange");
    channel.queueDeclarePassive("nfvo.manager.handling");
    channel.basicQos(1);

    channel.basicPublish("openbaton-exchange", "nfvo.manager.handling", null, message.getBytes());
    channel.close();
    connection.close();
  }
}
