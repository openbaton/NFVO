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

package org.openbaton.nfvo.security.authentication;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Autowired
  @Qualifier("customUserDetailsService")
  private UserDetailsManager customUserDetailsManager;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(customUserDetailsManager).passwordEncoder(new BCryptPasswordEncoder());
  }

  @Override
  public void init(WebSecurity web) {
    web.ignoring().antMatchers("/");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    //      http.authorizeRequests().antMatchers("/login").permitAll().and()
    //              .authorizeRequests().anyRequest().authenticated();
    http.antMatcher("/**")
        .authorizeRequests()
        .anyRequest()
        .authenticated()
        .and()
        .csrf()
        .and()
        .cors();
  }

  @Override
  @Bean
  protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }

  @Bean(name = "inMemManager")
  public UserDetailsManager userDetailsManager() {
    return new InMemoryUserDetailsManager(new Properties());
  }

  @Bean
  public FilterRegistrationBean simpleCorsFilter() {
    UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowCredentials(false);
    corsConfiguration.addAllowedOrigin("*");
    corsConfiguration.addAllowedHeader("*");
    corsConfiguration.addAllowedMethod("*");
    corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
    FilterRegistrationBean bean =
        new FilterRegistrationBean(new CorsFilter(corsConfigurationSource));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }

  /** For enabling annotations like @PreAuthorize("hasAnyRole('ROLE_ADMIN')") on RestControllers. */
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  private static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration {
    public GlobalSecurityConfiguration() {}

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
      return new OAuth2MethodSecurityExpressionHandler();
    }
  }
}
