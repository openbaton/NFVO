/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
 */

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
public abstract class VimDriver implements ClientInterfaces {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected Properties properties;

    protected VimDriver() {
        loadProperties();
    }

    public void loadProperties() {
        properties = new Properties();

        log.debug("Loading properties");
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/plugin.conf.properties");
            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
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
