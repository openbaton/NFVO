package org.project.openbaton.common.vnfm_sdk.utils;


import org.project.openbaton.catalogue.nfvo.VnfmManagerEndpoint;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by lto on 27/05/15.
 * TODO need to be moved to the right place in order to provide like an sdk to implement a vnfm_reg for JMS
 */
public class UtilsJMS {
    private static String server = "localhost";

    // that is the JavaNaming and Directory Interface --> service to find names
    // objects in java
    private static Context jndiContext = null;

    // this factory creates the connection to the queues
    private static ConnectionFactory connectionFactory = null;

    // that is the connection we use for connecting to the queues
    private static Connection connection = null;

    // the JMS Session
    private static Session session = null;

    // that is the destination from which we want to read the messages
    private static Destination destinationQueue = null;

    // that is the Consumer that receives the messages
    private static MessageProducer messageProducer = null;

    public static void sendToQueue(Serializable endpoint, String queueName) throws NamingException, JMSException {
        init(queueName);
        connection = connectionFactory.createConnection();
        connection.start();
        // in the session you can set password, transaction-Mode, etc.
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // this consumer listens to the destination queue with a specific
        // filter:
        messageProducer = session.createProducer(destinationQueue);
        ObjectMessage objMessage = session.createObjectMessage(endpoint);
        messageProducer.send(objMessage);
    }

    public static void sendToRegister(VnfmManagerEndpoint endpoint) throws NamingException, JMSException {
        init("org.project.openbaton.common.vnfm_sdk-register");

        // Connect to ActiveMQ
        connection = connectionFactory.createConnection();
        connection.start();
        // in the session you can set password, transaction-Mode, etc.
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // this consumer listens to the destination queue with a specific
        // filter:
        messageProducer = session.createProducer(destinationQueue);
        ObjectMessage objMessage = session.createObjectMessage(endpoint);
        messageProducer.send(objMessage);
    }

    private static void init(String queueName) throws NamingException {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://" + server + ":61616");
        // get the context from the ActiveMQ Broker
        jndiContext = new InitialContext(props);
        connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
        destinationQueue = (Destination) jndiContext.lookup("dynamicQueues/" + queueName);
    }
}
