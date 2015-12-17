package org.openbaton.plugin.utils;

import org.openbaton.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 26/11/15.
 */
@Service
public class RabbitPluginBroker {

    @Autowired
    private ConfigurableApplicationContext context;
    
    static Logger log = LoggerFactory.getLogger(RabbitPluginBroker.class);

    public Object getVimDriverCaller(String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("vimDriverCaller", type);
    }

    public Object getVimDriverCaller(String name, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("vimDriverCaller", name, type);
    }

    public Object getVimDriverCaller(String brokerIp, int port, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("vimDriverCaller", brokerIp, port, type);
    }

    public Object getVimDriverCaller(String brokerIp, String username, String password, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("vimDriverCaller", brokerIp, username, password, type);
    }

    public Object getMonitoringPluginCaller(String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", type);
    }

    public Object getMonitoringPluginCaller(String name, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", name, type);
    }

    public Object getMonitoringPluginCaller(String brokerIp, int port, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", brokerIp, port, type);
    }

    public Object getMonitoringPluginCaller(String brokerIp, String username, String password, String type) throws TimeoutException, IOException, NotFoundException {
        return context.getBean("monitoringPluginCaller", brokerIp, username, password, type);
    }

}
