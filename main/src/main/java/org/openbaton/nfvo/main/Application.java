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

package org.openbaton.nfvo.main;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.openbaton.nfvo.plugin.utils.PluginStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.util.FileSystemUtils;

import javax.jms.ConnectionFactory;
import java.io.File;

/**
 * Created by lto on 16/04/15.
 */


@SpringBootApplication
@EnableJms
@ConditionalOnClass(ActiveMQConnectionFactory.class)
@EntityScan(basePackages = "org.openbaton")
@ComponentScan(basePackages = "org.openbaton")
@EnableJpaRepositories("org.openbaton")
public class Application implements ApplicationListener<ContextClosedEvent> {

    public static void main(String[] args) {
        // Clean out any ActiveMQ data from a previous run
        FileSystemUtils.deleteRecursively(new File("activemq-data"));
        Logger log = LoggerFactory.getLogger(Application.class);

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        context.registerShutdownHook();
        log.info("Started OpenBaton");

        for (String name : context.getBeanDefinitionNames())
            log.trace(name);
    }

    @Bean
    JmsListenerContainerFactory<?> queueJmsContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setCacheLevelName("CACHE_CONNECTION");
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
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

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        PluginStartup.destroy();
    }
}
