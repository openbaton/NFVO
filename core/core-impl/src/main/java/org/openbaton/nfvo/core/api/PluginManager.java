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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.nfvo.common.utils.rabbit.RabbitManager;
import org.openbaton.plugin.mgmt.PluginStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PluginManager implements org.openbaton.nfvo.core.interfaces.PluginManager {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.plugin.active.consumers:10}")
  private String numConsumers;

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${spring.rabbitmq.virtual-host:/}")
  private String virtualHost;

  @Value("${nfvo.rabbit.management.port:15672}")
  private String managementPort;

  @Value("${spring.rabbitmq.host:localhost}")
  private String brokerIp;

  @Value("${nfvo.plugin.installation-dir:./plugins}")
  private String pluginDir;

  @Value("${nfvo.plugin.wait:true}")
  private boolean waitForPlugin;

  @Value("${nfvo.plugin.install:true}")
  private boolean installPlugin;

  @Value("${spring.config.location:/etc/openbaton/openbaton-nfvo.properties}")
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
    String pluginName = name;
    String filename = name.toLowerCase() + ".jar";
    String id = type + "/" + name + "/" + version;

    for (String pluginId : listInstalledVimDrivers()) {
      if (pluginId.contains(type)) {
        throw new AlreadyExistingException("Plugin of type " + type + " is already installed");
      }
    }

    String url = "http://" + marketIp + ":" + marketPort + "/api/v1/vim-drivers/" + id + "/jar";
    String path = pluginDir + "/install-plugin/";
    log.info("Download URL: " + url);

    File installDir = new File(pluginDir + "/install-plugin");

    if (!installDir.exists()) {
      installDir.mkdirs();
    }

    URL pluginURL = new URL(url);
    URLConnection conn = pluginURL.openConnection();
    String headerField = conn.getHeaderField("Content-Disposition");
    if (headerField != null && headerField.contains("filename=\"")) {
      filename =
          headerField.substring(headerField.indexOf("filename=\"") + 10, headerField.length() - 1);
      String[] split = filename.split("-");
      if (split.length > 4) {
        pluginName = split[3];
      } else if (split.length > 2) {
        pluginName = split[split.length - 2];
      } else {
        if (pluginName.contains("-")) pluginName = pluginName.substring(0, pluginName.indexOf("-"));
      }
    }
    path += filename;
    try (FileOutputStream out = new FileOutputStream(path);
        BufferedInputStream fileInputStream = new BufferedInputStream(conn.getInputStream())) {
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
    }
    startPlugin(path, pluginName);
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
        virtualHost,
        "" + managementPort,
        pluginLogPath,
        waitForPlugin);
  }

  @Override
  public Set<String> listInstalledVimDrivers() throws IOException {
    Set<String> result = new HashSet<>();
    for (String pluginId :
        RabbitManager.getQueues(
            brokerIp, username, password, virtualHost, Integer.parseInt(managementPort))) {
      if (pluginId.startsWith("vim-driver")) result.add(pluginId);
    }
    return result;
  }
}
