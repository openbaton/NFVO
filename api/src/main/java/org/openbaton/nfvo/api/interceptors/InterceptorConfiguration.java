/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/** Created by lto on 25/05/16. */
@Configuration
//@EnableWebMvc
public class InterceptorConfiguration extends WebMvcConfigurerAdapter {

  @Autowired private AuthorizeInterceptor authorizeInterceptor;
  @Autowired private HistoryInterceptor historyInterceptor;

  //    @Override
  //    public void addResourceHandlers(ResourceHandlerRegistry registry) {
  //        registry.addResourceHandler("/gui/**").addResourceLocations("/static/**");
  //        registry.addResourceHandler("/error").addResourceLocations("/static/login.html");
  //    }
  //
  //    @Bean
  //    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
  //        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
  //        mapping.setInterceptors(new Object[] {getAuthorizeInterceptor()});
  //        return mapping;
  //    }
  //    @Bean
  //    public AuthorizeInterceptor getAuthorizeInterceptor(){
  //        return new AuthorizeInterceptor();
  //    }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {

    registry
        .addInterceptor(authorizeInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/oauth/token");
    registry
        .addInterceptor(historyInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/oauth/token");
  }
  //
  //    @Override
  //    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
  //        configurer.enable();
  //    }
  //
  //    @Override
  //    public void addResourceHandlers(ResourceHandlerRegistry registry) {
  //        registry.addResourceHandler("/**").addResourceLocations("/static/**/");
  //    }
}
