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

package org.openbaton.nfvo.cli;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.security.User;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.WrongStatusException;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.openbaton.nfvo.repositories.NetworkServiceDescriptorRepository;
import org.openbaton.nfvo.repositories.NetworkServiceRecordRepository;
import org.openbaton.nfvo.repositories.UserRepository;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.openbaton.plugin.mgmt.PluginStartup;
import org.openbaton.utils.rabbit.RabbitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A Bridge for either executing the openbaton shell standalone or in an existing spring boot
 * environment that either leads to calls of the main() or the run() method.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@ConfigurationProperties(prefix = "nfvo.rabbit")
public class OpenbatonCLI implements CommandLineRunner {

  private static final Map<String, String> helpCommandList =
      new HashMap<String, String>() {
        {
          //mgmt
          put("help", "Print the usage");
          put("exit", "Exit the application");
          put("installPlugin", "install a plugin");
          put("uninstallPlugin", "uninstall a plugin");
          put("listPlugins", "list all registered plugin");
          put("listBeans", "list all registered Beans");
          put("listVnfms", "list all registered Vnfms");
          put("listUsers", "list all Users");
          put("changeLog", "Change log level");

          //admin
          put("listDescriptors", "show all Network Service Descriptors");
          put("listRecords", "show all Network Service Records");
          put("deleteRecord", "delete the Network Service Record with given id");
          put("deleteDescriptor", "delete the Network Service Descriptor with given id");
        }
      };
  private Logger log = LoggerFactory.getLogger(this.getClass());

  private String brokerIp;

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${nfvo.rabbit.management.port:15672}")
  private int managementPort;

  @Autowired private ConfigurableApplicationContext context;
  @Autowired private VnfmEndpointRepository vnfmEndpointRepository;

  @Value("${nfvo.plugin.log.path:./plugin-logs}")
  private String pluginLogPath;

  @Autowired private UserRepository userRepository;
  @Autowired private NetworkServiceRecordRepository nsrRepository;
  @Autowired private NetworkServiceDescriptorRepository nsdRepository;
  @Autowired private NetworkServiceRecordManagement nsrManagement;
  @Autowired private NetworkServiceDescriptorManagement nsdManagement;

  private static void exit(int status) {
    System.exit(status);
  }

  private static void usage() {
    System.out.println("/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/");
    System.out.println("Usage: java -jar build/libs/openbaton-<$version>.jar");
    System.out.println("Available commands are");

    for (Entry<String, String> entry : helpCommandList.entrySet()) {
      String format = "%-80s%s%n";
      System.out.printf(format, entry.getKey() + ":", entry.getValue());
    }
    System.out.println("/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/");
  }

  /**
   * Base start as a single module
   *
   * @param args parameters for starting the shell and bootstrap
   */
  public static void main(String[] args) {
    OpenbatonCLI openbatonCLI = new OpenbatonCLI();
    try {
      openbatonCLI.run(args);
    } catch (Exception e) {
      openbatonCLI.log.error(e.getMessage());
    }
  }

  public int getManagementPort() {
    return managementPort;
  }

  public void setManagementPort(int managementPort) {
    this.managementPort = managementPort;
  }

  public String getBrokerIp() {
    return brokerIp;
  }

  public void setBrokerIp(String brokerIp) {
    this.brokerIp = brokerIp;
  }

  /**
   * When running in spring boot application this implements the CommandLineRunner and is executed
   * after all the spring-shell components were loaded.
   *
   * @param args parameters for starting the shell and bootstrap
   */
  @Override
  public void run(String... args) throws Exception {
    if (!Arrays.asList(args).contains("--no-console")) {
      ConsoleReader reader = null;
      try {
        reader = new ConsoleReader();
      } catch (IOException ignored) {
        log.error("Oops, Error while creating ConsoleReader");
        exit(999);
      }
      String line;
      PrintWriter out = new PrintWriter(reader.getOutput());
      List<Completer> completors = new ArrayList<>();
      completors.add(new StringsCompleter(helpCommandList.keySet()));
      completors.add(new FileNameCompleter());
      reader.addCompleter(new ArgumentCompleter(completors));
      reader.setPrompt(
          "\u001B[135m" + System.getProperty("user.name") + "@[\u001B[32mopen-baton\u001B[0m]~> ");
      while ((line = reader.readLine()) != null) {
        out.flush();
        line = line.trim();
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
          exit(0);
        } else if (line.equalsIgnoreCase("listBeans")) {
          for (String name : context.getBeanDefinitionNames()) {
            System.out.println(name);
          }
        } else if (line.equalsIgnoreCase("cls")) {
          reader.clearScreen();
        } else if (line.equalsIgnoreCase("help")) {
          usage();
        } else if (line.startsWith("installPlugin ")) {
          installPlugin(line);
        } else if (line.startsWith("uninstallPlugin ")) {
          uninstallPlugin(line);
        } else if (line.startsWith("listVnfms")) {
          listVnfms();
        } else if (line.startsWith("listUsers")) {
          listUsers();
        } else if (line.startsWith("changeLog")) {
          changeLog(line);
        } else if (line.startsWith("listRecords")) {
          listRecords();
        } else if (line.startsWith("listDescriptors")) {
          listDescriptors();
        } else if (line.startsWith("deleteRecord")) {
          deleteRecord(line);
        } else if (line.startsWith("deleteDescriptor")) {
          deleteDescriptor(line);
        } else if (line.startsWith("listPlugins")) {
          StringTokenizer stringTokenizer = new StringTokenizer(line);
          stringTokenizer.nextToken();
          if (stringTokenizer.hasMoreTokens()) {
            System.out.println(listPlugins());
          } else {
            System.out.println(listPlugins());
          }
        } else if (line.equalsIgnoreCase("")) {
        } else {
          usage();
        }
      }
    }
  }

  private void deleteDescriptor(String line) throws WrongStatusException, EntityInUseException {
    StringTokenizer stringTokenizer = new StringTokenizer(line, " ");

    if (stringTokenizer.countTokens() != 2) {
      System.err.println("Error: please provide only the id to be removed");
    }
    stringTokenizer.nextToken();
    String id = stringTokenizer.nextToken();
    nsdManagement.delete(id, nsdRepository.findFirstById(id).getProjectId());
  }

  private void deleteRecord(String line) throws NotFoundException, WrongStatusException {
    StringTokenizer stringTokenizer = new StringTokenizer(line, " ");

    if (stringTokenizer.countTokens() != 2) {
      System.err.println("Error: please provide only the id to be removed");
    }

    stringTokenizer.nextToken();
    String id = stringTokenizer.nextToken();

    nsrManagement.delete(id, nsrRepository.findFirstById(id).getProjectId());
  }

  private void listRecords() {

    String result = "\n";
    result += "Available NSRs:\n";
    result +=
        String.format(
                "+%40s+%20s+%40s+",
                "----------------------------------------",
                "--------------------",
                "----------------------------------------")
            + "\n";
    result += String.format("|%40s|%20s|%40s|", "id", "name", "project-id") + "\n";
    result +=
        String.format(
                "+%40s+%20s+%40s+",
                "========================================",
                "====================",
                "========================================")
            + "\n";

    for (NetworkServiceRecord networkServiceRecord : nsrRepository.findAll()) {
      result +=
          String.format(
                  "|%40s|%20s|%40s|",
                  networkServiceRecord.getId(),
                  networkServiceRecord.getName(),
                  networkServiceRecord.getProjectId())
              + "\n";
      result +=
          String.format(
                  "+%40s+%20s+%40s+",
                  "----------------------------------------",
                  "--------------------",
                  "----------------------------------------")
              + "\n";
    }
    System.out.println(result);
  }

  private void listDescriptors() {

    String result = "\n";
    result += "Available NSRs:\n";
    result +=
        String.format(
                "+%40s+%20s+%40s+",
                "----------------------------------------",
                "--------------------",
                "----------------------------------------")
            + "\n";
    result += String.format("|%40s|%20s|%40s|", "id", "name", "project-id") + "\n";
    result +=
        String.format(
                "+%40s+%20s+%40s+",
                "========================================",
                "====================",
                "========================================")
            + "\n";

    for (NetworkServiceDescriptor networkServiceDescriptor : nsdRepository.findAll()) {
      result +=
          String.format(
                  "|%40s|%20s|%40s|",
                  networkServiceDescriptor.getId(),
                  networkServiceDescriptor.getName(),
                  networkServiceDescriptor.getProjectId())
              + "\n";
      result +=
          String.format(
                  "+%40s+%20s+%40s+",
                  "----------------------------------------",
                  "--------------------",
                  "----------------------------------------")
              + "\n";
    }
    System.out.println(result);
  }

  private void changeLog(String line) {
    StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
    if (stringTokenizer.countTokens() == 3) {
      stringTokenizer.nextToken();
      ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(stringTokenizer.nextToken()))
          .setLevel(Level.toLevel(stringTokenizer.nextToken()));
    } else if (stringTokenizer.countTokens() == 2) {
      stringTokenizer.nextToken();
      ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.openbaton"))
          .setLevel(Level.toLevel(stringTokenizer.nextToken()));
    } else {
      System.out.println("usage: changeLog [<package>] <level>");
      System.out.println("where <package> default is: org.openbaton");
      System.out.println("where <level> is:");
      System.out.println("\t * INFO");
      System.out.println("\t * DEBUG");
      System.out.println("\t * TRACE");
    }
  }

  private void listUsers() {
    String line = String.format("%20s%20s%40s", "Username", "Enabled", "Roles");
    System.out.println(line);
    for (User user : userRepository.findAll()) {
      System.out.println(
          String.format("%20s%20s%40s", user.getUsername(), user.isEnabled(), user.getRoles()));
    }
  }

  private void listVnfms() {

    String line = String.format("%20s%20s%20s", "Vnfm type", "active", "enable");

    System.out.println(line);
    for (VnfmManagerEndpoint endpoint : vnfmEndpointRepository.findAll()) {
      System.out.println(
          String.format(
              "%20s%20s%20s", endpoint.getType(), endpoint.isActive(), endpoint.isEnabled()));
    }
  }

  private void uninstallPlugin(String line) {
    StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
    stringTokenizer.nextToken(); // uninstallPlugin
    String pluginId = stringTokenizer.nextToken();

    PluginStartup.uninstallPlugin(pluginId);
  }

  private String listPlugins() throws IOException {
    String result = "\n";
    result += "Available plugins:\n";
    result +=
        String.format(
                "+%20s+%20s+%20s+",
                "--------------------", "--------------------", "--------------------")
            + "\n";
    result += String.format("|%20s|%20s|%20s|", "plugin type", "tool type", "plugin name") + "\n";
    result +=
        String.format(
                "+%20s+%20s+%20s+",
                "====================", "====================", "====================")
            + "\n";
    System.out.println();
    //    for (Entry<String, Process> entry : PluginStartup.getProcesses().entrySet()) {
    //      result += String.format("%40s", entry.getKey()) + "\n";
    //    }

    for (String queue : RabbitManager.getQueues(brokerIp, username, password, managementPort)) {
      if (queue.startsWith("vim-drivers") || queue.startsWith("monitoring")) {
        StringTokenizer stringTokenizer = new StringTokenizer(queue, ".");

        result +=
            String.format(
                    "|%20s|%20s|%20s|",
                    stringTokenizer.nextToken(),
                    stringTokenizer.nextToken(),
                    stringTokenizer.nextToken())
                + "\n";
        result +=
            String.format(
                    "+%20s+%20s+%20s+",
                    "--------------------", "--------------------", "--------------------")
                + "\n";
      }
    }
    return result;
  }

  private boolean installPlugin(String line) throws IOException {
    StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
    stringTokenizer.nextToken(); // installPlugin
    String path;
    if (stringTokenizer.hasMoreTokens()) {
      path = stringTokenizer.nextToken();
    } else {
      log.error("please provide path and name");
      return false;
    }
    String name = null;
    if (stringTokenizer.hasMoreTokens()) {
      name = stringTokenizer.nextToken();
    } else {
      log.error("please provide path and name");
      return false;
    }
    int consumers;
    if (stringTokenizer.hasMoreTokens()) {
      consumers = Integer.parseInt(stringTokenizer.nextToken());
    } else {
      log.error("please provide number of active consumers");
      return false;
    }
    if (stringTokenizer.hasMoreTokens()) {
      username = stringTokenizer.nextToken();
      if (!stringTokenizer.hasMoreTokens()) {
        log.error("please provide password too");
        return false;
      }
      password = stringTokenizer.nextToken();
    } else {
      if (username == null || username.equals("")) {
        username = "admin";
      }
      if (password == null || password.equals("")) {
        password = "openbaton";
      }
    }

    if (!pluginLogPath.endsWith("/")) {
      pluginLogPath += "/";
    }
    PluginStartup.installPlugin(
        name,
        path,
        "localhost",
        "5672",
        consumers,
        username,
        password,
        "" + managementPort,
        pluginLogPath,
        true);
    return true;
  }
}
