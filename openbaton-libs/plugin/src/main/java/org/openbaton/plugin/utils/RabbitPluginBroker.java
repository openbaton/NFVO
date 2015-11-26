package org.openbaton.plugin.utils;

import org.openbaton.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 26/11/15.
 */
@Service
@Scope
public class RabbitPluginBroker {

    @Autowired
    private static ConfigurableApplicationContext context;
    static Logger log = LoggerFactory.getLogger(RabbitPluginBroker.class);

    public static Object getVimDriverCaller(String type) throws TimeoutException, IOException, NotFoundException {
        log.debug("context is: " + context);
        return context.getBean("vimDriverCaller", type);
    }

    public static  Object getVimDriverCaller(String name, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("vimDriverCaller", name, type);
    }

    public static  Object getVimDriverCaller(String brokerIp, int port, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("vimDriverCaller", brokerIp, port, type);
    }

    public static  Object getVimDriverCaller(String brokerIp, String username, String password, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("vimDriverCaller", brokerIp, username, password, type);
    }

    public static  Object getMonitoringPluginCaller(String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", type);
    }

    public static  Object getMonitoringPluginCaller(String name, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", name, type);
    }

    public static  Object getMonitoringPluginCaller(String brokerIp, int port, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", brokerIp, port, type);
    }

    public static  Object getMonitoringPluginCaller(String brokerIp, String username, String password, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", brokerIp, username, password, type);
    }

}
