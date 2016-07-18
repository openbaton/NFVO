package org.openbaton.nfvo.api.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by lto on 25/05/16.
 */
@Configuration
//@EnableWebMvc
public class InterceptorConfiguration extends WebMvcConfigurerAdapter {

  @Autowired private AuthorizeInterceptor interceptor;

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

    registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns("/oauth/token");
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
