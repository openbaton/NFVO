/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.plugin.utils;

import com.google.gson.*;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.codec.binary.Base64;
import org.openbaton.catalogue.nfvo.PluginMessage;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimDriverException;
import org.openbaton.nfvo.common.configuration.NfvoGsonDeserializerImage;
import org.openbaton.nfvo.common.configuration.NfvoGsonDeserializerNetwork;
import org.openbaton.nfvo.common.configuration.NfvoGsonDeserializerVimInstance;
import org.openbaton.nfvo.common.configuration.NfvoGsonSerializerVimInstance;
import org.openbaton.nfvo.common.utils.rabbit.RabbitManager;
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
  private Gson gson =
      new GsonBuilder()
          .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
          .registerTypeAdapter(BaseNetwork.class, new NfvoGsonDeserializerNetwork())
          .registerTypeAdapter(BaseNfvImage.class, new NfvoGsonDeserializerImage())
          .registerTypeAdapter(BaseVimInstance.class, new NfvoGsonDeserializerVimInstance())
          .registerTypeAdapter(BaseVimInstance.class, new NfvoGsonSerializerVimInstance())
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
      throws IOException, NotFoundException {
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
      throws IOException, NotFoundException {
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

    try (Connection connection = factory.newConnection()) {
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
        throw new PluginException("Plugin with id: " + pluginId + " not existing anymore...");
      }
      if (returnType != null) {
        String res = response.poll(this.timeout, TimeUnit.MILLISECONDS);
        if (res == null) {
          throw new PluginException(
              String.format("Plugin did not responded after %d milliseconds", this.timeout));
        }
        JsonObject jsonObject = gson.fromJson(res, JsonObject.class);

        JsonElement exceptionJson = jsonObject.get("exception");
        if (exceptionJson == null) {
          JsonElement answerJson = jsonObject.get("answer");

          Serializable ret;
          if (answerJson == null) {
            throw new PluginException(
                "Plugin return null without throwing exception... It is hard to understand for me what went wrong, i am"
                    + " only a (free) sofware...");
          }
          if (answerJson.isJsonPrimitive()) {
            ret = gson.fromJson(answerJson.getAsJsonPrimitive(), returnType);
          } else if (answerJson.isJsonArray()) {
            ret = gson.fromJson(answerJson.getAsJsonArray(), returnType);
          } else {
            ret = gson.fromJson(answerJson.getAsJsonObject(), returnType);
          }

          log.trace("answer is: " + ret);
          return ret;
        } else {
          PluginException pluginException;
          try {
            pluginException =
                new PluginException(
                    gson.fromJson(exceptionJson.getAsJsonObject(), VimDriverException.class));
            log.error("Got Vim Driver Exception with cause: " + pluginException.getMessage());
            log.error(
                "Got Vim Driver Exception with server: "
                    + ((VimDriverException) pluginException.getCause()).getServer());
          } catch (Exception ignored) {
            channel.queueDelete(replyQueueName);
            pluginException =
                new PluginException(
                    gson.fromJson(exceptionJson.getAsJsonObject(), Throwable.class));
          }
          throw pluginException;
        }
      } else {
        try {
          channel.queueDelete(replyQueueName);
        } catch (Exception ignored) {
        }
        return null;
      }
    } catch (TimeoutException e) {
      throw new PluginException("Could not open a connection after timeout.");
    }
  }
}
