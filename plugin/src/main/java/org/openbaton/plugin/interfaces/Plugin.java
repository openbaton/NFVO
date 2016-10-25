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

package org.openbaton.plugin.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by mpa on 26.10.15.
 */
public abstract class Plugin {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  protected Properties properties;

  protected Plugin() {
    super();
    //        loadProperties();
  }

  public void loadProperties() {
    properties = new Properties();
    log.trace("Loading properties");
    try {
      properties.load(this.getClass().getResourceAsStream("/plugin.conf.properties"));
      if (properties.getProperty("external-properties-file") != null) {
        File externalPropertiesFile = new File(properties.getProperty("external-properties-file"));
        if (externalPropertiesFile.exists()) {
          log.debug(
              "Loading properties from external-properties-file: "
                  + properties.getProperty("external-properties-file"));
          InputStream is = new FileInputStream(externalPropertiesFile);
          properties.load(is);
        } else {
          log.debug(
              "external-properties-file: "
                  + properties.getProperty("external-properties-file")
                  + " doesn't exist");
        }
      }
    } catch (Exception ignored) {

    }
    log.debug("Loaded properties: " + properties);
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public String getType() {
    return properties.getProperty("type", "type not defined");
  }
}
