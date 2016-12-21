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

package org.openbaton.nfvo.main;

import org.openbaton.plugin.mgmt.PluginStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Created by lto on 16/04/15. */
@SpringBootApplication
@EntityScan(basePackages = "org.openbaton")
@ComponentScan(basePackages = "org.openbaton")
@EnableJpaRepositories(value = "org.openbaton")
@EnableScheduling
public class Application implements ApplicationListener<ContextClosedEvent> {

  private static Logger log = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {

    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    context.registerShutdownHook();
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    log.info("Shutting down...");
    PluginStartup.destroy();
  }
}
