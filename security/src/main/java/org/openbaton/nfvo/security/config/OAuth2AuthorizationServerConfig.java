/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.security.config;

import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;
import org.openbaton.nfvo.security.authentication.CustomClientDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableAuthorizationServer
@ConfigurationProperties
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  public static final String RESOURCE_ID = "oauth2-server";
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private AuthenticationManager authenticationManager;

  private TokenStore tokenStore = new InMemoryTokenStore();

  private CustomClientDetailsService customClientDetailsService;

  @Value("${nfvo.security.user.token.validity:1200}")
  private int userTokenValidityDuration;

  @Value("${nfvo.security.service.token.validity:31556952}")
  private int serviceTokenValidityDuration;

  @Value("${nfvo.security.image.token.validity:15}")
  private int imageTokenValidityDuration;

  private DefaultTokenServices serviceTokenServices;
  private DefaultTokenServices imageTokenServices;

  @PostConstruct
  public void init() {
    serviceTokenServices = new DefaultTokenServices();
    serviceTokenServices.setSupportRefreshToken(true);
    serviceTokenServices.setTokenStore(tokenStore);
    serviceTokenServices.setAccessTokenValiditySeconds(serviceTokenValidityDuration);

    imageTokenServices = new DefaultTokenServices();
    imageTokenServices.setSupportRefreshToken(false);
    imageTokenServices.setTokenStore(tokenStore);
    imageTokenServices.setAccessTokenValiditySeconds(imageTokenValidityDuration);
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    endpoints.authenticationManager(this.authenticationManager).tokenStore(this.tokenStore);
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer client) throws Exception {
    customClientDetailsService = new CustomClientDetailsService();
    BaseClientDetails openbatonOSClient = buildOpenBatonOSClient();
    customClientDetailsService.addclientDetails(openbatonOSClient);
    client.withClientDetails(customClientDetailsService);
  }

  /**
   * Method for generating an OAuth2 token for services. The token's (and refresh token's) validity
   * duration is longer than for normal users.
   *
   * @param serviceName
   * @return the oauth2 service token
   */
  public OAuth2AccessToken getNewServiceToken(String serviceName) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ADMIN"));

    OAuth2Request oAuth2Request = buildOAuth2Request(serviceName, authorities);
    User userPrincipal =
        new User(serviceName, "" + Math.random() * 1000, true, true, true, true, authorities);

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

    BaseClientDetails externalServiceClientDetails = buildExternalServiceClientDetails(serviceName);
    customClientDetailsService.addclientDetails(externalServiceClientDetails);

    OAuth2AccessToken token = serviceTokenServices.createAccessToken(auth);
    log.trace("New Service token: " + token);
    return token;
  }

  /**
   * Method returns a token that can be used to request a specific image file contained in the
   * NFVImage repository from the REST API.
   *
   * @param imageId ID of the image that can be retrieved with the token
   * @return the oauth2 token for fetching image files from the image repository
   */
  public String getNewImageToken(String imageId) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority(imageId));

    OAuth2Request oAuth2Request = buildOAuth2Request("vimdriver" + imageId, authorities);

    User userPrincipal =
        new User(
            "vimdriver" + imageId, "" + Math.random() * 1000, true, true, true, true, authorities);

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

    OAuth2AccessToken token = imageTokenServices.createAccessToken(auth);
    return token.getValue();
  }

  /**
   * Validates an image token against an image ID. If the token is able to grant access to the image
   * file, this method returns true otherwise false.
   *
   * @param token the token passed to the REST API
   * @param imageId ID of the NFVImage
   * @return
   */
  public boolean validateImageToken(String token, String imageId) {
    OAuth2AccessToken imageToken = imageTokenServices.readAccessToken(token);
    if (imageToken == null || imageToken.isExpired() || !imageToken.getScope().contains(imageId))
      return false;
    return true;
  }

  /**
   * Revokes the passed image token.
   *
   * @param token the token to revoke
   */
  public void removeImageToken(String token) {
    imageTokenServices.revokeToken(token);
  }

  private BaseClientDetails buildOpenBatonOSClient() {
    BaseClientDetails openbatonOSClient =
        new BaseClientDetails(
            "openbatonOSClient", RESOURCE_ID, "read,write", "refresh_token,password", "ADMIN");
    openbatonOSClient.setClientSecret("secret");
    openbatonOSClient.setAccessTokenValiditySeconds(userTokenValidityDuration);
    return openbatonOSClient;
  }

  private BaseClientDetails buildExternalServiceClientDetails(String serviceName) {
    BaseClientDetails externalServiceClientDetails =
        new BaseClientDetails(serviceName, "", "read,write", "refresh_token,password", "ADMIN");
    externalServiceClientDetails.setAccessTokenValiditySeconds(serviceTokenValidityDuration);
    return externalServiceClientDetails;
  }

  private OAuth2Request buildOAuth2Request(String serviceName, Set<GrantedAuthority> authorities) {
    Map<String, String> requestParameters = new HashMap<>();
    Set<String> scopes = new HashSet<>(Arrays.asList("read", "write"));
    Set<String> resourceIds = new HashSet<>();
    Set<String> responseTypes = new HashSet<>();
    responseTypes.add("code");
    Map<String, Serializable> extensionProperties = new HashMap<>();

    return new OAuth2Request(
        requestParameters,
        serviceName,
        authorities,
        true,
        scopes,
        resourceIds,
        null,
        responseTypes,
        extensionProperties);
  }
}
