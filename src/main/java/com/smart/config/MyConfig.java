package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity
public class MyConfig  {

	@Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImp();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public DaoAuthenticationProvider authenticationProvider() {
//		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//		daoAuthenticationProvider.setUserDetailsService(getUserDetailsService());
//		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//		return daoAuthenticationProvider;
//	}
//
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		// TODO Auto-generated method stub
//		auth.authenticationProvider(authenticationProvider());
//	}
//
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		// TODO Auto-generated method stub.
//		http.authorizeRequests().antMatchers("/seller/**").hasRole("SELLER")
//		.antMatchers("/customer/**").hasRole("CUSTOMER").antMatchers("/**").permitAll().
//		and().formLogin().loginPage("/signin").defaultSuccessUrl("/redirect");
//	}
	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception
	{
	
		http.authorizeRequests().antMatchers("/seller/**").hasRole("SELLER")
		.antMatchers("/customer/**").hasRole("CUSTOMER").antMatchers("/**").permitAll().
		and().formLogin().loginPage("/signin").loginProcessingUrl("/signed").defaultSuccessUrl("/redirect").and().csrf().disable();
		return http.build();
	}
	

}
