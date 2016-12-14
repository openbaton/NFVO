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

package org.openbaton.nfvo.repositories.tests;

import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

/** Created by lto on 30/04/15. */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "org.openbaton")
@ComponentScan(basePackages = "org.openbaton")
@EnableJpaRepositories(basePackages = "org.openbaton")
public class ApplicationTest {

  /** Main method for testing if the context contains all the needed beans */
  public static void main(String[] argv) {
    ConfigurableApplicationContext context = SpringApplication.run(ApplicationTest.class);
    for (String s : context.getBeanDefinitionNames()) System.out.println(s);
  }

  @Bean
  public DataSource dataSource() {
    // instantiate, configure and return embedded DataSource
    return new EmbeddedDatabaseBuilder().build();
  }
}
