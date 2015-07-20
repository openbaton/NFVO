/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

package org.project.openbaton.cli;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.nfvo.vim_interfaces.vim.VimBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;

/**
 * A Bridge for either executing the openbaton shell standalone or in an existing
 * spring boot environment that either leads to calls of the main() or the run()
 * method.
 */
@Component
public class OpenbatonCLI implements CommandLineRunner {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VimBroker vimBroker;

    private static final Character mask = '*';

    private static void exit(int status) {
        System.exit(status);
    }
    private final static Map<String, String> helpCommandList = new HashMap<String, String>(){{
        put("help", "Print the usage");
        put("exit", "Exit the application");
        put("install", "install plugin");
        put("print properties", "print all the properties");
    }};

    /**
	 * When running in spring boot application this implements the CommandLineRunner 
	 * and is executed after all the spring-shell components were loaded.
	 * 
	 * @param args
	 *            parameters for starting the shell and bootstrap
	 */
    @Override
    public void run(String... args) throws Exception {

//		Bootstrap bootstrap = new Bootstrap();
//        List<String> argumentList = new ArrayList<String>(Arrays.asList(args));
//        bootstrap.main(argumentList.toArray(new String[0]));

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
        completors.add(new StringsCompleter(new ArrayList<String>(){{
            add("test");
            add("openstack");
            add("amazon");
        }}));
        reader.addCompleter(new ArgumentCompleter(completors));
        reader.setPrompt("\u001B[135m" + System.getProperty("user.name") + "@[\u001B[32mopen-baton\u001B[0m]~> ");
        while ((line = reader.readLine()) != null) {
            out.flush();
            line = line.trim();
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                exit(0);
            }else
            if (line.equalsIgnoreCase("cls")) {
                reader.clearScreen();
            }else
            if (line.equalsIgnoreCase("help")) {
                usage();
            }else
            if (line.startsWith("install ")) {
                installPlugin(line);
            }else
            if (line.equalsIgnoreCase("")) {
                continue;
            }else usage();
        }
    }

    private boolean installPlugin(String line) {
        String path = line.split(" ")[1];
        String type = line.split(" ")[2];
        File jar = new File(path);
        if (!jar.exists())
            return false;

        ClassLoader parent = ClassUtils.getDefaultClassLoader();
        path = jar.getAbsolutePath();
        try {
            log.debug("path is: " + path);
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file://" + path)}, parent);

            URL url = classLoader.getResource("org/project/openbaton/clients/interfaces/client/test/TestClient.class");
            System.out.println("URL: " + url.toString());
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            JarFile file = connection.getJarFile();
            switch (type){
                case "test":
                    Class c = classLoader.loadClass("org.project.openbaton.clients.interfaces.client.test.TestClient");
                    ClientInterfaces instance = (ClientInterfaces) c.newInstance();
                    log.debug("instance: " + instance);
                    vimBroker.addClient(instance, type);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void usage() {
        System.out.println("/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/");
        System.out.println("Usage: java -jar build/libs/openbaton-<$version>.jar");
        System.out.println("Available commands are");
        for (Object entry : helpCommandList.entrySet()) {
            System.out.println("\t" + ((Map.Entry)entry).getKey() + ":\t" + ((Map.Entry)entry).getValue());
        }
        System.out.println("/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/");
    }
    /**
     * Base start as a single module
     *
     * @param args
     *            parameters for starting the shell and bootstrap
     */
    public static void main(String[] args) {
        OpenbatonCLI openbatonCLI = new OpenbatonCLI();
        try {
            openbatonCLI.run(args);
        } catch (Exception e) {
            openbatonCLI.log.error(e.getMessage(), e);
        }
    }
}