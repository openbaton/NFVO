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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
@ConfigurationProperties(prefix = "nfvo.security")
public class ResourceServer extends ResourceServerConfigurerAdapter {

    private boolean enabled;
    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void configure(HttpSecurity http) throws Exception {
//        http
//                .requestMatchers().antMatchers("/api/v1/", "/api/v1/**")
//                .and()
//                .authorizeRequests().anyRequest().access("#oauth2.hasScope('write')");
//
//
//        http
//                .anonymous()
//                .disable();

        // API calls

        if (enabled) {
            log.debug("Security is enabled");
            http
                    .authorizeRequests()
                    .regexMatchers(HttpMethod.POST, "/api/v1/")
                    .access("#oauth2.hasScope('write')")
                    .and()
                            //.addFilterBefore(clientCredentialsTokenEndpointFilter(), BasicAuthenticationFilter.class)
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
                            //.addFilterBefore(clientCredentialsTokenEndpointFilter(), BasicAuthenticationFilter.class)
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                    .and()
                    .exceptionHandling();
        }else {
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

}