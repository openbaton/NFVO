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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

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
   * @param rabbitTemplate The spring RabbitTemplate @see
   *     org.springframework.amqp.rabbit.core.RabbitTemplate
   * @param endpoint The VNFM Endpoint to register @see
   *     org.openbaton.catalogue.nfvo.VnfmManagerEndpoint
   * @return new username and password for rabbitmq for specific vnfm endpoint
   */
  public String[] registerVnfmToNfvo(RabbitTemplate rabbitTemplate, VnfmManagerEndpoint endpoint) {

    JsonObject message = new JsonObject();
    message.add("type", new JsonPrimitive(endpoint.getType()));
    message.add("action", new JsonPrimitive("register"));
    message.add("vnfmManagerEndpoint", gson.toJsonTree(endpoint, VnfmManagerEndpoint.class));
    log.debug("Registering the Vnfm to the Nfvo");

    Object res =
        rabbitTemplate.convertSendAndReceive(
            "openbaton-exchange", "nfvo.manager.handling", gson.toJson(message));

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
   * @param rabbitTemplate The spring rabbit template @see
   *     org.springframework.amqp.rabbit.core.RabbitTemplate
   * @param endpoint the VNfm Endpoint to remove @see
   *     org.openbaton.catalogue.nfvo.VnfmManagerEndpoint
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
   * @param brokerIp the rabbitmq broker ip
   * @param port the port of rabbitmq
   * @param username the username to connect with
   * @param password the password to connect with
   * @param virtualHost the virtual host of rabbitmq
   * @param pluginName the name of the plugin
   * @return the new ManagerCredentials @see org.openbaton.catalogue.nfvo.ManagerCredentials
   * @throws IOException In case of InterruptedException
   * @throws TimeoutException in case of TimeoutException
   * @throws InterruptedException in case of InterruptedException
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
        new AMQP.BasicProperties.Builder()
            .correlationId(corrId)
            .contentType("text/plain")
            .replyTo(replyQueueName)
            .build();

    final BlockingQueue<ManagerCredentials> response = new ArrayBlockingQueue<>(1);

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

  /**
   * Sends a deregistration message to the NFVO
   *
   * @param brokerIp the rabbitmq broker ip
   * @param port the port of rabbitmq
   * @param username the username to connect with
   * @param password the password to connect with
   * @param virtualHost the virtualHost
   * @param managerCredentialUsername the username to remove
   * @param managerCredentialPassword the password to remove
   * @throws IOException In case of InterruptedException
   * @throws TimeoutException in case of TimeoutException
   */
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

    AMQP.BasicProperties props =
        new AMQP.BasicProperties.Builder().contentType("text/plain").build();

    channel.basicPublish("openbaton-exchange", "nfvo.manager.handling", props, message.getBytes());
    channel.close();
    connection.close();
  }

  public static void main(String[] args)
      throws InterruptedException, TimeoutException, IOException {
    new Registration()
        .registerPluginToNfvo("localhost", 5672, "admin", "openbaton", "/", "cippalippa");
  }

  /**
   * Returns true if the Registration object's username field is not null and has a value different
   * than the empty string. Can be therefore used to check if the registration was successful or
   * not.
   *
   * @return true if username is not null and not empty
   */
  public boolean hasUsername() {
    if (this.username != null && !this.username.equals("")) return true;
    return false;
  }
}
