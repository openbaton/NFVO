package org.openbaton.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.openbaton.catalogue.nfvo.PluginAnswer;
import org.openbaton.catalogue.nfvo.PluginMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 25/11/15.
 */
public abstract class PluginCaller {

    private final QueueingConsumer consumer;
    private final String pluginId;
    private final String exchange = "plugin-exchange";
    private String replyQueueName;
    private Channel channel;
    private Connection connection;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PluginCaller(String pluginId, String brokerIp, String username, String password, int port) throws IOException, TimeoutException {
        this.pluginId = pluginId;
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
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    protected Serializable executeRPC(String methodName, Collection<Serializable> args) throws IOException, InterruptedException {
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

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response = new String(delivery.getBody());
                break;
            }
        }

        return gson.fromJson(response, PluginAnswer.class).getAnswer();
    }

}
