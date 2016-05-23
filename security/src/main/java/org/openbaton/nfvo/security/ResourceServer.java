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

package org.openbaton.nfvo.security;

import org.openbaton.catalogue.security.User;
import org.openbaton.nfvo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
@ConfigurationProperties(prefix = "nfvo.security")
public class ResourceServer extends ResourceServerConfigurerAdapter implements CommandLineRunner {

    private boolean enabled;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private UserRepository userRepository;
    @Value("${nfvo.security.admin.password:openbaton}")
    private String adminPwd;
    @Value("${nfvo.security.guest.password:guest}")
    private String guestPwd;


    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.headers()
                .frameOptions().disable();

        // API calls

        if (enabled) {
            log.debug("Security is enabled");
            http
                    .authorizeRequests()
                    .regexMatchers(HttpMethod.POST, "/api/v1/")
                    .access("#oauth2.hasScope('write')")
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                    .and()
                    .exceptionHandling();

            // API calls
            http
                    .authorizeRequests()
                    .antMatchers("/api/**")
                    .access("#oauth2.hasScope('write')")
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                    .and()
                    .exceptionHandling();
        } else {
            log.warn("Security is not enabled!");
            http
                    .authorizeRequests().anyRequest().permitAll();
        }
    }


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(OAuth2AuthorizationServerConfig.RESOURCE_ID);
    }


    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) throws Exception {

        log.debug("Creating initial Users...");

        Iterable<User> users = userRepository.findAll();
        boolean found = false;
        for (User user : users)
            if (user.getUsername().equals("admin"))
                found = true;

        if (!found) {
            User userAdmin = new User();
            userAdmin.setAdmin(true);
            userAdmin.setUsername("admin");
            userAdmin.setFirstName("Admin");
            userAdmin.setPassword(BCrypt.hashpw(adminPwd, BCrypt.gensalt(12)));
            userAdmin.setLastName("OpenBaton");
            userRepository.save(userAdmin);
        }

        found = false;

        for (User user : users)
            if (user.getUsername().equals("guest"))
                found = true;

        if (!found) {
            User userGuest = new User();
            userGuest.setAdmin(false);
            userGuest.setFirstName("Guest");
            userGuest.setUsername("guest");
            userGuest.setPassword(BCrypt.hashpw(guestPwd, BCrypt.gensalt(12)));
            userGuest.setLastName("Guest");
            userRepository.save(userGuest);
        }
    }
}