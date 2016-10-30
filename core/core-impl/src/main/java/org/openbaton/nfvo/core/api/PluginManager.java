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

package org.openbaton.nfvo.core.api;

import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.plugin.utils.PluginStartup;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rvl on 19.10.16.
 */
@Service
public class PluginManager implements org.openbaton.nfvo.core.interfaces.PluginManager {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.plugin.active.consumers:10}")
  private String numConsumers;

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${nfvo.rabbit.management.port:15672}")
  private String managementPort;

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String brokerIp;

  @Value("${nfvo.plugin.installation-dir:./plugins}")
  private String pluginDir;

  @Value("${nfvo.plugin.wait:true}")
  private boolean waitForPlugin;

  @Value("${nfvo.plugin.install:true}")
  private boolean installPlugin;

  @Value("${spring.config.location:/etc/openbaton/openbaton.properties}")
  private String propFileLocation;

  @Value("${nfvo.plugin.log.path:./plugin-logs}")
  private String pluginLogPath;

  @Value("${nfvo.marketplace.ip:marketplace.openbaton.org}")
  private String marketIp;

  @Value("${nfvo.marketplace.port:8082}")
  private int marketPort;

  @Override
  public void downloadPlugin(String type, String name, String version)
      throws IOException, AlreadyExistingException {

    String id = type + "/" + name + "/" + version;

    for (String pluginId : listInstalledVimDrivers()) {
      if (pluginId.contains(type)) {
        throw new AlreadyExistingException("Plugin of type " + type + " is already installed");
      }
    }

    String url = "http://" + marketIp + ":" + marketPort + "/api/v1/vim-drivers/" + id + "/jar";
    String path = pluginDir + "/install-plugin/" + name + ".jar";
    log.info("Download URL: " + url);

    File installDir = new File(pluginDir + "/install-plugin");

    if (!installDir.exists()) {
      installDir.mkdirs();
    }

    URL pluginURL = new URL(url);
    FileOutputStream out = new FileOutputStream(path);
    try {
      BufferedInputStream fileInputStream = new BufferedInputStream(pluginURL.openStream());
      byte[] buf = new byte[8192];
      int bytesread = 0, bytesBuffered = 0;
      while ((bytesread = fileInputStream.read(buf)) > -1) {
        out.write(buf, 0, bytesread);
        bytesBuffered += bytesread;
        if (bytesBuffered > 1024 * 1024) { //flush after 1MB
          bytesBuffered = 0;
          out.flush();
        }
      }
    } finally {
      if (out != null) {
        out.flush();
      }
    }

    startPlugin(path, name);
  }

  @Override
  public void startPlugin(String path, String name) throws IOException {

    PluginStartup.installPlugin(
        name,
        path,
        "localhost",
        "5672",
        Integer.parseInt(numConsumers),
        username,
        password,
        "" + managementPort,
        pluginLogPath);
  }

  @Override
  public Set<String> listInstalledVimDrivers() throws IOException {
    Set<String> result = new HashSet<>();
    for (String pluginId :
        RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort))) {
      if (pluginId.startsWith("vim-driver")) result.add(pluginId);
    }
    return result;
  }
}
