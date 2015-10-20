package org.openbaton.vim.drivers.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;


/**
 * Created by lto on 15/10/15.
 */
public abstract class VimDriver extends UnicastRemoteObject implements ClientInterfaces {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected Properties properties;

    protected VimDriver() throws RemoteException {
        loadProperties();
    }

    public void loadProperties() {
        properties = new Properties();

        log.debug("Loading properties");
        try {
            properties.load(this.getClass().getResourceAsStream("/plugin.conf.properties"));
            if (properties.getProperty("external-properties-file") != null) {
                File externalPropertiesFile = new File(properties.getProperty("external-properties-file"));
                if (externalPropertiesFile.exists()) {
                    log.debug("Loading properties from external-properties-file: " + properties.getProperty("external-properties-file"));
                    InputStream is = new FileInputStream(externalPropertiesFile);
                    properties.load(is);
                } else {
                    log.debug("external-properties-file: " + properties.getProperty("external-properties-file") + " doesn't exist");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("Loaded properties: " + properties);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
