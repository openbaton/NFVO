package org.openbaton.plugin.utils;

import com.google.gson.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.openbaton.catalogue.nfvo.PluginMessage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 25/11/15.
 */
public class PluginCaller {

    private final QueueingConsumer consumer;
    private final String pluginId;
    private final String exchange = "plugin-exchange";
    private final String brokerIp;
    private final String username;
    private final String password;
    private String replyQueueName;
    private Channel channel;
    private Connection connection;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public PluginCaller(String pluginId, String brokerIp, String username, String password, int port) throws IOException, TimeoutException, NotFoundException {
        this.pluginId = getFullPluginId(pluginId, brokerIp, username, password);
        this.brokerIp = brokerIp;
        this.username = username;
        this.password = password;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(brokerIp);
        if (username != null)
            factory.setUsername(username);
        else
            factory.setUsername("admin");
        if (password != null)
            factory.setPassword(password);
        else
            factory.setPassword("openbaton");
        if (port > 1024)
            factory.setPort(port);
        else
            factory.setPort(5672);
        connection = factory.newConnection();
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
        channel.queueBind(replyQueueName,exchange,replyQueueName);
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    private String getFullPluginId(String pluginId, String brokerIp, String username, String password) throws IOException, NotFoundException {
        List<String> queues = RabbitManager.getQueues(brokerIp, username, password);
        for (String queue: queues){
            if (queue.startsWith(pluginId))
                return queue;
        }
        throw new NotFoundException("no plugin found with name: " + pluginId);
    }

    public Serializable executeRPC(String methodName, Collection<Serializable> args, Type returnType) throws IOException, InterruptedException, PluginException {

        //Check if plugin is still up

        if (!RabbitManager.getQueues(brokerIp,username,password).contains(pluginId))
            throw new PluginException("Plugin with id: " + pluginId + " not existing anymore...");

        String response;
        String corrId = java.util.UUID.randomUUID().toString();
        PluginMessage pluginMessage = new PluginMessage();
        pluginMessage.setMethodName(methodName);
        pluginMessage.setParameters(args);
        String message = gson.toJson(pluginMessage);

        BasicProperties props = new BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish(exchange, pluginId, props, message.getBytes());

        if (returnType != null) {

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {

                    response = new String(delivery.getBody());
                    log.trace("received: " + response);
                    break;
                }
            }

            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
            JsonArray answerArray = null;
            JsonObject answerObject = null;
            JsonPrimitive answerPrimitive = null;


            JsonElement answerJson = jsonObject.get("answer");

            Serializable ret=null;

            if (answerJson.isJsonPrimitive()){
                ret = gson.fromJson(answerJson.getAsJsonPrimitive(), returnType);
            }else if (answerJson.isJsonArray()){
                ret = gson.fromJson(answerJson.getAsJsonArray(), returnType);
            }else
                ret = gson.fromJson(answerJson.getAsJsonObject(), returnType);

            log.trace("answer is: " + ret);
            return ret;

        }
        else
            return null;
    }

}
