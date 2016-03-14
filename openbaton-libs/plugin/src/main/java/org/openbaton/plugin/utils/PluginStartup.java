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

package org.openbaton.plugin.utils;

import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lto on 10/09/15.
 */
public class PluginStartup {

    private static Logger log = LoggerFactory.getLogger(PluginStartup.class);

    private static Map<String, Process> processes = new HashMap<>();

    public static void installPlugin(String name, String path, String brokerIp, String port, int consumers, String username, String password, String managementPort) throws IOException {
        List<String> queues = RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
        //java -jar build/libs/openstack-plugin-0.10-SNAPSHOT.jar openstack localhost 5672 10 username password
        log.debug("Running: java -jar " + path + " " + name + " localhost " + port + " " + consumers + " " + username + " *****");
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", path, name, brokerIp, port, "" + consumers, username, password);
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        File dir = new File("./plugin-logs/");
        if (!dir.exists())
            dir.mkdirs();
        File file = new File("plugin-logs/plugin-" + name + "_" + ft.format(dNow) + ".log");
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(file));
        log.trace("ProcessBuilder is: " + processBuilder);
        Process p = processBuilder.start();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
            return;
        }
        List<String> queuesNew = RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
        int waitTime = 30;
        while (queuesNew.size() != queues.size() + 1) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error(e.getLocalizedMessage());
                return;
            }
            queuesNew = RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
            waitTime--;
            if (waitTime == 0){
                log.error("After 30 seconds the plugin is not started.");
            }
        }

        for (String pluginId : queuesNew) {
            if (!queues.contains(pluginId)) {
                processes.put(pluginId, p);
            }
        }
    }

    private synchronized static void installPlugin(String path, boolean waitForPlugin, String brokerIp, String port, int consumers, String username, String password, String managementPort) throws IOException {
        List<String> queues = null;
        if (waitForPlugin) {
            queues = RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
        }
        String pluginName = path.substring(path.lastIndexOf("/") + 1, path.length());
        String name = pluginName.substring(0, pluginName.indexOf("-"));
        log.debug("Running: java -jar " + path + " " + name + " localhost " + port + " " + consumers + " " + username + " *****");
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", path, name, brokerIp, port, "" + consumers, username, password);
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        File dir = new File("./plugin-logs/");
        if (!dir.exists())
            dir.mkdirs();
        File file = new File("plugin-logs/plugin-" + name + "_" + ft.format(dNow) + ".log");
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.to(file));
        log.trace("ProcessBuilder is: " + processBuilder);
        Process p = processBuilder.start();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
            return;
        }
        if (waitForPlugin) {
            List<String> queuesNew = RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
            int waitTime = 30;
            while (queuesNew.size() != queues.size() + 1) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                    return;
                }
                queuesNew = RabbitManager.getQueues(brokerIp, username, password, Integer.parseInt(managementPort));
                waitTime--;
                if (waitTime == 0){
                    log.error("After 15 seconds the plugin is not started.");
                }
            }
            for (String pluginId : queuesNew) {
                if (!queues.contains(pluginId)) {
                    processes.put(pluginId, p);
                }
            }
        }
        else
            processes.put(path, p);
    }

    public static void startPluginRecursive(String folderPath, boolean waitForPlugin, String registryip, String port, int consumers, String username, String password, String managementPort) throws IOException {

        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            for (File jar : folder.listFiles()) {
                if (jar.getAbsolutePath().endsWith(".jar"))
                    installPlugin(jar.getAbsolutePath(), waitForPlugin, registryip, port, consumers, username, password, managementPort);
                else if (jar.isDirectory())
                    startPluginRecursive(jar.getAbsolutePath(), waitForPlugin, registryip, port, consumers, username, password, managementPort);
                else
                    log.warn(jar.getAbsolutePath() + " is not a jar file");
            }
        } else log.error(folderPath + " must be a folder");

    }

    public static void destroy() {
        for (Process p : processes.values()) {
            p.destroy();
        }
    }

    public static void uninstallPlugin(String pluginId) {
        processes.get(pluginId).destroy();
    }
}
