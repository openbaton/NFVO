/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.plugin.mgmt;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openbaton.plugin.utils.Utils;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by lto on 10/09/15. */
public class PluginStartup {

  private static Logger log = LoggerFactory.getLogger(PluginStartup.class);
  private static Map<String, Process> processes = new HashMap<>();

  public static Map<String, Process> getProcesses() {
    return processes;
  }

  public static synchronized void installPlugin(
      String name,
      String path,
      String brokerIp,
      String port,
      int consumers,
      String username,
      String password,
      String managementPort,
      String pluginLogPath,
      boolean waitForPlugin)
      throws IOException {

    List<String> queues = null;
    if (waitForPlugin) {
      queues =
          RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
    }

    Process p =
        Utils.executePlugin(
            path, name, brokerIp, port, consumers, username, password, pluginLogPath);

    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
      log.error(e.getLocalizedMessage());
      return;
    }

    if (waitForPlugin) {
      List<String> queuesNew =
          RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
      int waitTime = 30;
      while (queuesNew.size() != queues.size() + 1) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          log.error(e.getLocalizedMessage());
          return;
        }
        queuesNew =
            RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
        waitTime--;
        if (waitTime == 0) {
          log.error("After 15 seconds the plugin is not started.");
          break;
        }
      }
      for (String pluginId : queuesNew) {
        if (!queues.contains(pluginId)) {
          processes.put(pluginId, p);
        } else processes.put(path, p);
      }
    }
  }

  public static void startPluginRecursive(
      String folderPath,
      boolean waitForPlugin,
      String registryip,
      String port,
      int consumers,
      String username,
      String password,
      String managementPort,
      String pluginLogPath)
      throws IOException {

    File folder = new File(folderPath);

    if (folder.isDirectory()) {
      for (File jar : folder.listFiles()) {
        if (jar.getAbsolutePath().endsWith(".jar")) {
          // quick workaround for avoiding a null pointer exception when plugin names is not like the expected one (pluginname-version)..
          String absolutePath = jar.getAbsolutePath();
          String pluginName =
              absolutePath.substring(absolutePath.lastIndexOf("/") + 1, absolutePath.length());

          String[] split = pluginName.split("-");
          if (split.length > 4) {
            pluginName = split[3];
          } else if (split.length > 2) {
            pluginName = split[split.length - 2];
          } else {
            if (pluginName.contains("-"))
              pluginName = pluginName.substring(0, pluginName.indexOf("-"));
          }

          installPlugin(
              pluginName,
              absolutePath,
              registryip,
              port,
              consumers,
              username,
              password,
              managementPort,
              pluginLogPath,
              waitForPlugin);
        } else if (jar.isDirectory()) {
          startPluginRecursive(
              jar.getAbsolutePath(),
              waitForPlugin,
              registryip,
              port,
              consumers,
              username,
              password,
              managementPort,
              pluginLogPath);
        } else log.warn(jar.getAbsolutePath() + " is not a jar file");
      }
    } else log.error(folderPath + " must be a folder");
  }

  public static void destroy() {
    for (Process p : processes.values()) {
      p.destroy();
    }
  }

  public static void uninstallPlugin(String pluginId) {
    Process process = processes.get(pluginId);
    if (process != null) process.destroy();
    else
      log.warn(
          "Not able to find any plugin with identifier: "
              + pluginId
              + ". Try one of the following: "
              + processes.keySet());
  }
}
