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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration
@EnableAuthorizationServer
@ConfigurationProperties
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  public static final String RESOURCE_ID = "oauth2-server";
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private AuthenticationManager authenticationManager;

  @Autowired
  @Qualifier("customUserDetailsService")
  private UserDetailsManager customUserDetailsManager;

  private DefaultTokenServices tokenServices;
  private DefaultTokenServices serviceTokenServices;
  private TokenStore tokenStore = new InMemoryTokenStore();

  @Value("${nfvo.security.user.token.validity:600}")
  private int userTokenValidityDuration;

  @Value("${nfvo.security.service.token.validity:31556952}")
  private int serviceTokenValidityDuration;

  @PostConstruct
  private void init() {
    this.serviceTokenServices = new DefaultTokenServices();
    this.serviceTokenServices.setSupportRefreshToken(true);
    this.serviceTokenServices.setTokenStore(tokenStore);
    this.serviceTokenServices.setAccessTokenValiditySeconds(serviceTokenValidityDuration);
  }

  public int getUserTokenValidityDuration() {
    return userTokenValidityDuration;
  }

  public void setUserTokenValidityDuration(int userTokenValidityDuration) {
    this.userTokenValidityDuration = userTokenValidityDuration;
  }

  @Bean
  public TokenStore tokenStore() {
    return new InMemoryTokenStore();
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints
        .authenticationManager(this.authenticationManager)
        .tokenStore(this.tokenStore)
        .userDetailsService(customUserDetailsManager);
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer client) throws Exception {
    client
        .inMemory()
        .withClient("openbatonOSClient")
        .secret("secret")
        .authorizedGrantTypes("authorization_code", "refresh_token", "password")
        .scopes("read", "write")
        //.authorities("ROLE_CLIENT")
        .resourceIds(RESOURCE_ID);
  }

  @Bean
  @Primary
  public DefaultTokenServices tokenServices() {
    this.tokenServices = new DefaultTokenServices();
    this.tokenServices.setSupportRefreshToken(true);
    this.tokenServices.setTokenStore(this.tokenStore);
    this.tokenServices.setAccessTokenValiditySeconds(userTokenValidityDuration);
    return tokenServices;
  }

  //
  //  @Bean
  //  public DefaultTokenServices serviceTokenServices() {
  //    DefaultTokenServices tokenServices = new DefaultTokenServices();
  //    tokenServices.setSupportRefreshToken(true);
  //    tokenServices.setTokenStore(tokenStore);
  //    tokenServices.setAccessTokenValiditySeconds(serviceTokenValidityDuration);
  //    return tokenServices;
  //  }

  public OAuth2AccessToken getNewServiceToken(String serviceName) {
    Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
    authorities.add(new SimpleGrantedAuthority("ADMIN"));

    Map<String, String> requestParameters = new HashMap<>();
    boolean approved = true;
    Set<String> scope = new HashSet<>();
    scope.add("write");
    scope.add("read");
    Set<String> resourceIds = new HashSet<>();
    Set<String> responseTypes = new HashSet<>();
    responseTypes.add("code");
    Map<String, Serializable> extensionProperties = new HashMap<>();

    OAuth2Request oAuth2Request =
        new OAuth2Request(
            requestParameters,
            serviceName,
            authorities,
            true,
            scope,
            resourceIds,
            null,
            responseTypes,
            extensionProperties);

    User userPrincipal =
        new User(serviceName, "" + Math.random() * 1000, true, true, true, true, authorities);

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

    //          DefaultTokenServices tokenServices = new DefaultTokenServices();
    //          tokenServices.setSupportRefreshToken(true);
    //          tokenServices.setTokenStore(tokenStore);
    //          tokenServices.setAccessTokenValiditySeconds(serviceTokenValidityDuration);

    OAuth2AccessToken token = serviceTokenServices.createAccessToken(auth);
    log.trace("New Service token: " + token);
    return token;
  }
}
