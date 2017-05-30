package org.openbaton.common.vnfm_sdk.amqp;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.springframework.stereotype.Component;

/** Created by tbr on 26.05.17. */
@Component
public class RabbitNfvMessageListener implements Callable<String> {

  private Gson gson;

  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Channel channel;
  private String queue;
  public long deliveryTag;
  private AMQP.BasicProperties messageProperties;

  public RabbitNfvMessageListener(
      String queue,
      String brokerIp,
      int brokerPort,
      String rabbitUsername,
      String rabbitPassword,
      String virtualHost) {
    this.queue = queue;
    this.gson = new Gson();
    this.connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(brokerIp);
    connectionFactory.setPort(brokerPort);
    connectionFactory.setUsername(rabbitUsername);
    connectionFactory.setPassword(rabbitPassword);
    connectionFactory.setVirtualHost(virtualHost);
  }

  private static String getStringFromInputStream(InputStream is) {

    BufferedReader br = null;
    StringBuilder sb = new StringBuilder();

    String line;
    try {

      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return sb.toString();
  }

  @Override
  public String call() throws IOException, TimeoutException {

    this.connection = connectionFactory.newConnection();
    this.channel = connection.createChannel();

    final BlockingQueue<String> communication = new ArrayBlockingQueue<String>(1);

    channel.basicConsume(
        queue,
        false,
        new DefaultConsumer(channel) {

          @Override
          public void handleDelivery(
              String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
              throws IOException {
            messageProperties = properties;
            try {
              String incomingObject = null;
              ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
              incomingObject = getStringFromInputStream(byteArrayInputStream);
              //              JsonObject jsonMessage = gson.fromJson(incomingObject, JsonObject.class);
              //              NFVMessage message = null;
              //              if (jsonMessage.get("action").getAsString().equals("INSTANTIATE"))
              //                message = gson.fromJson(incomingObject, OrVnfmInstantiateMessage.class);
              //              else System.out.println("Received: " + jsonMessage.get("action").getAsString());
              deliveryTag = envelope.getDeliveryTag();
              //              communication.offer(message);
              communication.offer(incomingObject);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });

    String nfvMessage;
    try {
      nfvMessage = communication.take();
    } catch (InterruptedException e) {
      channel.close();
      connection.close();
      return null;
    }

    return nfvMessage;
  }

  public void rpcReply(NFVMessage nfvMessage) {
    if (this.channel == null && !this.channel.isOpen())
      throw new RuntimeException("Channel is null or closed");

    AMQP.BasicProperties replyProps =
        new AMQP.BasicProperties.Builder()
            .correlationId(messageProperties.getCorrelationId())
            .build();
    try {
      String replyTo = messageProperties.getReplyTo();
      String answer = gson.toJson(nfvMessage, NFVMessage.class);
      this.channel.basicPublish("", replyTo, replyProps, answer.getBytes("UTF-8"));
      this.channel.basicAck(deliveryTag, false);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      this.channel.close();
      this.connection.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }
}
