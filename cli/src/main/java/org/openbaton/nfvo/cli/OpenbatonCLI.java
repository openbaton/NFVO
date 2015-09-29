/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.cli;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.io.FileUtils;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.InstallPluginEvent;
import org.openbaton.nfvo.repositories.ConfigurationRepository;
import org.openbaton.nfvo.repositories.PluginEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * A Bridge for either executing the openbaton shell standalone or in an existing
 * spring boot environment that either leads to calls of the main() or the run()
 * method.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class OpenbatonCLI implements CommandLineRunner, ApplicationEventPublisherAware {

    private final static Map<String, String> helpCommandList = new HashMap<String, String>() {{
        put("help", "Print the usage");
        put("exit", "Exit the application");
        put("installVim", "install vim driver plugin");
        put("installMonitor", "install monitoring plugin");
        put("print properties", "print all the properties");
    }};
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PluginEndpointRepository pluginEndpointRepository;
    private ApplicationEventPublisher publisher;

    @Autowired
    private ConfigurationRepository configurationRepository;

    private static void exit(int status) {
        System.exit(status);
    }

    public static void usage() {
        System.out.println("/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/");
        System.out.println("Usage: java -jar build/libs/openbaton-<$version>.jar");
        System.out.println("Available commands are");
        for (Object entry : helpCommandList.entrySet()) {
            System.out.println("\t" + ((Map.Entry) entry).getKey() + ":\t" + ((Map.Entry) entry).getValue());
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

    /**
     * When running in spring boot application this implements the CommandLineRunner
     * and is executed after all the spring-shell components were loaded.
     *
     * @param args parameters for starting the shell and bootstrap
     */
    @Override
    public void run(String... args) throws Exception {

        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
        } catch (IOException e) {
            log.error("Oops, Error while creating ConsoleReader");
            exit(999);
        }
        String line;
        PrintWriter out = new PrintWriter(reader.getOutput());
        List<Completer> completors = new LinkedList<>();
        completors.add(new StringsCompleter(helpCommandList.keySet()));
        completors.add(new FileNameCompleter());
        reader.addCompleter(new ArgumentCompleter(completors));
        reader.setPrompt("\u001B[135m" + System.getProperty("user.name") + "@[\u001B[32mopen-baton\u001B[0m]~> ");
        while ((line = reader.readLine()) != null) {
            out.flush();
            line = line.trim();
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                exit(0);
            } else if (line.equalsIgnoreCase("cls")) {
                reader.clearScreen();
            } else if (line.equalsIgnoreCase("help")) {
                usage();
            } else if (line.startsWith("installVim ")) {
                installPlugin(line, "vim");
            } else if (line.startsWith("installMonitor ")) {
                installPlugin(line, "monitor");
            } else if (line.equalsIgnoreCase("")) {
                continue;
            } else usage();
        }
    }

    private boolean installPlugin(String line, String type) throws IOException {
        String path = line.split(" ")[1];
        List<String> classes = new ArrayList<>();
        File jar = new File(path);
        if (!jar.exists() || jar.isDirectory()) {
            log.error(jar.getAbsolutePath() + " doesn't exists or is not a plugin.");
            return false;
        }

        path = jar.getAbsolutePath();
        log.debug("path is: " + path);

        String installPath = null;

        for (Configuration c : configurationRepository.findAll()) {
            if (c.getName().equals("system")) {
                for (ConfigurationParameter cp : c.getConfigurationParameters()) {
                    if (type.equals("vim")) {
                        if (cp.getConfKey().equals("vim-plugin-installation-dir")) {
                            installPath = cp.getValue();
                        }
                        if (cp.getConfKey().equals("vim-classes")) {
                            classes = Arrays.asList(cp.getValue().split(";"));
                        }
                    } else if (type.equals("monitor")) {
                        if (cp.getConfKey().equals("monitoring-plugin-installation-dir")) {
                            installPath = cp.getValue();
                        }
                        if (cp.getConfKey().equals("monitoring-classes")) {
                            classes = Arrays.asList(cp.getValue().split(";"));
                        }
                    }
                }
            }
        }
        String new_filename = installPath + jar.getName();
        File dest = new File(new_filename);
        log.debug("newFileNAme: " + new_filename);
        FileUtils.copyFile(jar, dest);
        InstallPluginEvent event = new InstallPluginEvent(this);
        event.setPath(new_filename);
        event.setType(type);
        event.setClasses(classes);
        this.publisher.publishEvent(event);
        return true;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}

