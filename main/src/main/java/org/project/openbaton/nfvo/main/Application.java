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

package org.project.openbaton.nfvo.main;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileSystemUtils;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 * Created by lto on 16/04/15.
 */


@SpringBootApplication
@EnableJms
@ConditionalOnClass(ActiveMQConnectionFactory.class)
@EntityScan(basePackages="org.project.openbaton.catalogue")
@ComponentScan(basePackages = {"org.project.openbaton.nfvo", "org.project.openbaton.cli"})
public class Application {

//    @Scheduled(fixedRate = 30000)
    private void contexrLoaderTask() {
        try {
            ClassLoader parent = ClassUtils.getDefaultClassLoader();
            System.out.println("Parent: " + parent);
            ClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:///opt/tmp/nfvo/plugins/clients-impl-0.2-SNAPSHOT.jar"), new URL("file:///opt/tmp/nfvo/plugins/jclouds-core-1.9.0.jar")/*, new URL("file:///opt/tmp/nfvo/openbaton-libs/vim-drivers/build/libs/vim-drivers-0.6-alpha.jar")*/}, parent);
            System.out.println("ClassLoader: " + classLoader);

            URL url = classLoader.getResource("org/project/openbaton/clients/interfaces/client/openstack/OpenstackClient.class");
            System.out.println("URL: " + url.toString());
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            JarFile file = connection.getJarFile();
            Class c = classLoader.loadClass("org.project.openbaton.clients.interfaces.client.openstack.OpenstackClient");
            ClientInterfaces instance = (ClientInterfaces) c.newInstance();
            System.out.println("Class: " + c);
            System.out.println("ClassName: " + c.getName());
            System.out.println("Instance: " + instance);
            String jarPath = file.getName();
            System.out.println(jarPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Bean
    JmsListenerContainerFactory<?> queueJmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    @Bean
    JmsListenerContainerFactory<?> topicJmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    public static void main(String[] args) {
        // Clean out any ActiveMQ data from a previous run
        FileSystemUtils.deleteRecursively(new File("activemq-data"));
        Logger log = LoggerFactory.getLogger(Application.class);

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        log.info("Started OpenBaton");

    }
    
  

}
