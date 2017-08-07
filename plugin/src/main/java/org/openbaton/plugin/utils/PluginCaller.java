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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import org.apache.commons.codec.binary.Base64;
import org.openbaton.catalogue.nfvo.PluginMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginCaller {

  private final String pluginId;
  private final String brokerIp;
  private final String username;
  private final String password;
  private final String virtualHost;
  private final long timeout;
  private ConnectionFactory factory;
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
      String virtualHost,
      int managementPort)
      throws TimeoutException, IOException, NotFoundException {
    this(pluginId, brokerIp, username, password, port, virtualHost, managementPort, 500000);
  }

  public PluginCaller(
      String pluginId,
      String brokerIp,
      String username,
      String password,
      int port,
      String virtualHost,
      int managementPort,
      long timeout)
      throws IOException, TimeoutException, NotFoundException {
    this.pluginId =
        getFullPluginId(pluginId, brokerIp, username, password, virtualHost, managementPort);
    this.managementPort = managementPort;
    this.brokerIp = brokerIp;
    this.username = username;
    this.password = password;
    this.virtualHost = virtualHost;
    this.timeout = timeout;
    factory = new ConnectionFactory();
    factory.setHost(brokerIp);
    if (username != null) {
      factory.setUsername(username);
    } else {
      factory.setUsername("admin");
    }
    if (password != null) {
      factory.setPassword(password);
    } else {
      factory.setPassword("openbaton");
    }
    if (port > 1024) {
      factory.setPort(port);
    } else {
      factory.setPort(5672);
    }
    if (virtualHost != null && !"".equals(virtualHost)) {
      factory.setVirtualHost(virtualHost);
    }
    //connection = factory.newConnection();

    //        replyQueueName = channel.queueDeclare().getQueue();
    //        channel.queueBind(replyQueueName,exchange,replyQueueName);
    //        consumer = new QueueingConsumer(channel);
    //        channel.basicConsume(replyQueueName, true, consumer);
  }

  public void close() throws IOException {
    connection.close();
  }

  private String getFullPluginId(
      String pluginId,
      String brokerIp,
      String username,
      String password,
      String virtualHost,
      int port)
      throws IOException, NotFoundException {
    List<String> queues = RabbitManager.getQueues(brokerIp, username, password, virtualHost, port);
    for (String queue : queues) {
      if (queue.startsWith(pluginId)) {
        return queue;
      }
    }
    throw new NotFoundException(
        "no plugin found with name: " + pluginId + " into queues: " + queues);
  }

  public Serializable executeRPC(String methodName, Collection<Serializable> args, Type returnType)
      throws IOException, InterruptedException, PluginException {

    try {
      connection = factory.newConnection();
    } catch (TimeoutException e) {
      throw new PluginException("Could not open a connection after timeout.");
    }
    Channel channel = connection.createChannel();
    String replyQueueName = channel.queueDeclare().getQueue();
    String exchange = "openbaton-exchange";
    channel.queueBind(replyQueueName, exchange, replyQueueName);
    String corrId = UUID.randomUUID().toString();
    BasicProperties props = new Builder().correlationId(corrId).replyTo(replyQueueName).build();

    PluginMessage pluginMessage = new PluginMessage();
    pluginMessage.setMethodName(methodName);
    pluginMessage.setParameters(args);

    String message = gson.toJson(pluginMessage);
    channel.basicPublish(exchange, pluginId, props, message.getBytes());

    final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

    //    QueueingConsumer consumer = new QueueingConsumer(channel);
    String consumerTag =
        channel.basicConsume(
            replyQueueName,
            true,
            new DefaultConsumer(channel) {
              @Override
              public void handleDelivery(
                  String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                  throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                  response.offer(new String(body, "UTF-8"));
                }
              }
            });

    //Check if plugin is still up
    if (!RabbitManager.getQueues(brokerIp, username, password, virtualHost, managementPort)
        .contains(pluginId)) {
      connection.close();
      throw new PluginException("Plugin with id: " + pluginId + " not existing anymore...");
    }
    if (returnType != null) {
      JsonObject jsonObject = gson.fromJson(response.take(), JsonObject.class);

      JsonElement exceptionJson = jsonObject.get("exception");
      if (exceptionJson == null) {
        JsonElement answerJson = jsonObject.get("answer");

        Serializable ret;

        if (answerJson.isJsonPrimitive()) {
          ret = gson.fromJson(answerJson.getAsJsonPrimitive(), returnType);
        } else if (answerJson.isJsonArray()) {
          ret = gson.fromJson(answerJson.getAsJsonArray(), returnType);
        } else {
          ret = gson.fromJson(answerJson.getAsJsonObject(), returnType);
        }

        log.trace("answer is: " + ret);
        connection.close();
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
          channel.queueDelete(replyQueueName);
          pluginException =
              new PluginException(gson.fromJson(exceptionJson.getAsJsonObject(), Throwable.class));
        }
        connection.close();
        throw pluginException;
      }
    } else {
      try {
        channel.queueDelete(replyQueueName);
      } catch (Exception ignored) {
      }
      connection.close();
      return null;
    }
  }
}
