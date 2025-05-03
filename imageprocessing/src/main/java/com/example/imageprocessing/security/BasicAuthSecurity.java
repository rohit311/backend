package com.example.imageprocessing.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.imageprocessing.services.UserService;

@Configuration
@EnableWebSecurity
public class BasicAuthSecurity {

  @Autowired
  private JwtRequestFilter jwtRequestFilter;

  @Autowired
  UserService userDetailsService;

  private final RestAuthenticationEntryPoint authenticationEntryPoint;

  @Autowired
  public BasicAuthSecurity(RestAuthenticationEntryPoint authenticationEntryPoint) {
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    /*http.csrf().disable();

    http.authorizeRequests()
        .requestMatchers("/login")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .httpBasic()
        .authenticationEntryPoint(authenticationEntryPoint);*/

      http.csrf(AbstractHttpConfigurer::disable);
      http.authorizeHttpRequests(request -> {
        request.requestMatchers("/login").permitAll();
        request.requestMatchers("/register").permitAll(); // it solve the problem
        request.anyRequest().authenticated();
      });

      http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth,PasswordEncoder passwordEncoder) throws Exception {
    auth.inMemoryAuthentication()
            .passwordEncoder(PasswordEncoderConfig.passwordEncoder())
            .withUser("user1")
            .password(PasswordEncoderConfig.passwordEncoder().encode("password"))
            .roles("ADMIN");
  }

  @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(PasswordEncoderConfig.passwordEncoder())
            .and()
            .build();
    }

}