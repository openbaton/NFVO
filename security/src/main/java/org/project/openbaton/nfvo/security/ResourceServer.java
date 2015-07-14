package org.project.openbaton.nfvo.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class ResourceServer extends ResourceServerConfigurerAdapter {

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
    }


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(OAuth2AuthorizationServerConfig.RESOURCE_ID);
    }

}