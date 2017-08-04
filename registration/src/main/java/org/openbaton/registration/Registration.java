package org.openbaton.registration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** This class handles the registration of Vnfms and plugins to the Nfvo. */
@Service
@Scope("prototype")
@ConfigurationProperties
public class Registration {

  private static Logger log = LoggerFactory.getLogger(Registration.class);
  private Gson gson;

  private String username;
  private String password;

  @Value("${vnfm.connect.tries:20}")
  private int maxTries;

  public Registration() {
    this.gson = new GsonBuilder().setPrettyPrinting().create();
  }

  /**
   * This method registers a Vnfm to the Nfvo by sending a request to the nfvo.manager.handling
   * queue using the given RabbitTemplate. The Nfvo's answer contains a username and password which
   * is injected into the RabbitTemplate so that it can send requests to the queue dedicated to the
   * specific Vnfm possessing the RabbitTemplate object.
   *
   * @param rabbitTemplate
   */
  public String[] registerVnfmToNfvo(RabbitTemplate rabbitTemplate, VnfmManagerEndpoint endpoint)
      throws InterruptedException {

    JsonObject message = new JsonObject();
    message.add("type", new JsonPrimitive(endpoint.getType()));
    message.add("action", new JsonPrimitive("register"));
    message.add("vnfmManagerEndpoint", gson.toJsonTree(endpoint, VnfmManagerEndpoint.class));
    log.debug("Registering the Vnfm to the Nfvo");
    int tries = 0;
    Object res = null;
    if (maxTries < 0) maxTries = Integer.MAX_VALUE;
    while (tries < maxTries) {
      res =
          rabbitTemplate.convertSendAndReceive(
              "openbaton-exchange", "nfvo.manager.handling", gson.toJson(message));
      if (res == null) {
        log.debug(
            "NFVO answer is null, i suppose it is not running yet, i will try again in 2,5 seconds.");
        Thread.sleep(2500);
        tries++;
      } else {
        break;
      }
    }
    if (res == null) {
      throw new IllegalArgumentException("The NFVO's answer to the registration request is null.");
    }
    if (!(res instanceof String)) {
      throw new IllegalArgumentException(
          "The NFVO's answer to the registration request should be of type String, but it is "
              + res.getClass().getSimpleName());
    }
    ManagerCredentials managerCredentials = gson.fromJson((String) res, ManagerCredentials.class);
    this.username = managerCredentials.getRabbitUsername();
    this.password = managerCredentials.getRabbitPassword();
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

    //    String replyQueueName = "amq.rabbitmq.reply-to";
    String replyQueueName = channel.queueDeclare().getQueue();
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
              String bodyString = new String(body);
              ManagerCredentials managerCredentials =
                  gson.fromJson(bodyString, ManagerCredentials.class);
              if (managerCredentials == null)
                throw new RuntimeException(
                    "Could not obtain credentials while registering plugin to Nfvo since the reply is no ManagerCredentials object");
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

  public static void main(String[] args)
      throws InterruptedException, TimeoutException, IOException {
    new Registration()
        .registerPluginToNfvo("localhost", 5672, "admin", "openbaton", "/", "cippalippa");
  }
}
