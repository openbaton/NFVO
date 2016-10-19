package org.openbaton.nfvo.core.api;

import org.openbaton.plugin.utils.PluginStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;

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

  @Override
  public void downloadPlugin(String type, String name, String version) throws IOException {

    String id = type + "/" + name + "/" + version;
    String url = "http://localhost:8082/api/v1/vim-drivers/" + id + "/jar";
    String path = "plugins/install-plugin/" + name + ".jar";
    log.info("Download URL: " + url);

    File installDir = new File("plugins/install-plugin");

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
}
