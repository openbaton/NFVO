//package org.project.openbaton.nfvo.gui;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled	=	true)
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//	@Autowired
//	private UserDetailsService userDetailsService;
//
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth)
//			throws Exception {
//		auth.userDetailsService(userDetailsService).passwordEncoder(
//				new BCryptPasswordEncoder());
//	}
//
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.sessionManagement()
//				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//				.and()
//				.authorizeRequests()
////				.antMatchers("/**").permitAll()
//				.antMatchers("/api/v1/**").authenticated().and().httpBasic()
//				.realmName("OpenBaton").and().csrf().disable();
//	}
//
//}
