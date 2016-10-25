/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.plugin.utils;

import com.google.gson.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import org.apache.commons.codec.binary.Base64;
import org.openbaton.catalogue.nfvo.PluginMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 25/11/15.
 */
public class PluginCaller {

  private final String pluginId;
  private final String brokerIp;
  private final String username;
  private final String password;
  private Connection connection;
  private Gson gson =
      new GsonBuilder()
          .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
          .setPrettyPrinting()
          .create();
  private Logger log = LoggerFactory.getLogger(this.getClass());

  private int managementPort;

  private static class ByteArrayToBase64TypeAdapter
      implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return Base64.decodeBase64(json.getAsString());
    }

    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(Base64.encodeBase64String(src));
    }
  }

  public PluginCaller(
      String pluginId,
      String brokerIp,
      String username,
      String password,
      int port,
      int managementPort)
      throws IOException, TimeoutException, NotFoundException {
    this.pluginId = getFullPluginId(pluginId, brokerIp, username, password, managementPort);
    this.managementPort = managementPort;
    this.brokerIp = brokerIp;
    this.username = username;
    this.password = password;
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(brokerIp);
    if (username != null) factory.setUsername(username);
    else factory.setUsername("admin");
    if (password != null) factory.setPassword(password);
    else factory.setPassword("openbaton");
    if (port > 1024) factory.setPort(port);
    else factory.setPort(5672);
    connection = factory.newConnection();

    //        replyQueueName = channel.queueDeclare().getQueue();
    //        channel.queueBind(replyQueueName,exchange,replyQueueName);
    //        consumer = new QueueingConsumer(channel);
    //        channel.basicConsume(replyQueueName, true, consumer);
  }

  public void close() throws IOException {
    connection.close();
  }

  private String getFullPluginId(
      String pluginId, String brokerIp, String username, String password, int port)
      throws IOException, NotFoundException {
    List<String> queues = RabbitManager.getQueues(brokerIp, username, password, port);
    for (String queue : queues) {
      if (queue.startsWith(pluginId)) return queue;
    }
    throw new NotFoundException(
        "no plugin found with name: " + pluginId + " into queues: " + queues);
  }

  public Serializable executeRPC(String methodName, Collection<Serializable> args, Type returnType)
      throws IOException, InterruptedException, PluginException {

    Channel channel = connection.createChannel();
    String replyQueueName = channel.queueDeclare().getQueue();
    String exchange = "plugin-exchange";
    channel.queueBind(replyQueueName, exchange, replyQueueName);
    QueueingConsumer consumer = new QueueingConsumer(channel);
    String consumerTag = channel.basicConsume(replyQueueName, true, consumer);

    //Check if plugin is still up
    if (!RabbitManager.getQueues(brokerIp, username, password, managementPort).contains(pluginId))
      throw new PluginException("Plugin with id: " + pluginId + " not existing anymore...");

    String response;
    String corrId = UUID.randomUUID().toString();
    PluginMessage pluginMessage = new PluginMessage();
    pluginMessage.setMethodName(methodName);
    pluginMessage.setParameters(args);
    String message = gson.toJson(pluginMessage);

    BasicProperties props = new Builder().correlationId(corrId).replyTo(replyQueueName).build();

    channel.basicPublish(exchange, pluginId, props, message.getBytes());

    if (returnType != null) {

      while (true) {
        Delivery delivery = consumer.nextDelivery();
        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
          response = new String(delivery.getBody());
          log.trace("received: " + response);
          break;
        } else {
          log.error("Received Message with wrong correlation id");
          throw new PluginException(
              "Received Message with wrong correlation id. This should not happen, if it does please call us.");
        }
      }

      channel.queueDelete(replyQueueName);
      try {
        channel.close();
      } catch (TimeoutException e) {
        e.printStackTrace();
      }
      JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

      JsonElement exceptionJson = jsonObject.get("exception");
      if (exceptionJson == null) {
        JsonElement answerJson = jsonObject.get("answer");

        Serializable ret = null;

        if (answerJson.isJsonPrimitive()) {
          ret = gson.fromJson(answerJson.getAsJsonPrimitive(), returnType);
        } else if (answerJson.isJsonArray()) {
          ret = gson.fromJson(answerJson.getAsJsonArray(), returnType);
        } else ret = gson.fromJson(answerJson.getAsJsonObject(), returnType);

        log.trace("answer is: " + ret);
        return ret;
      } else {
        PluginException pluginException;
        try {
          pluginException =
              new PluginException(
                  gson.fromJson(exceptionJson.getAsJsonObject(), VimDriverException.class));
          log.debug(
              "Got Vim Driver Exception with server: "
                  + ((VimDriverException) pluginException.getCause()).getServer());
        } catch (Exception ignored) {
          pluginException =
              new PluginException(gson.fromJson(exceptionJson.getAsJsonObject(), Throwable.class));
        }
        throw pluginException;
      }
    } else return null;
  }
}
