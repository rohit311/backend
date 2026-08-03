package com.example.oauth;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class OauthApplication {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		/*http
			.authorizeHttpRequests(auth -> auth
			.requestMatchers("/", "/index.html", "/error").permitAll()
			.requestMatchers("/webjars/**").permitAll()
			.requestMatchers("/oauth2/**", "/login/**").permitAll()
			.anyRequest().authenticated())
			.csrf(c -> c
            	.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        	)
			.logout(l -> l
            	.logoutSuccessUrl("/").permitAll()
        	)
			.oauth2Login(Customizer.withDefaults());*/

			http
				.authorizeHttpRequests(auth -> auth
					.requestMatchers("/", "/index.html", "/error", "/webjars/**").permitAll()
					.anyRequest().authenticated()
				)
				.oauth2Login(Customizer.withDefaults())
				.logout(logout -> logout
					.logoutSuccessUrl("/")
					.permitAll()
				)
				.csrf(csrf -> csrf
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				);


		return http.build();
	}

	@GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name", principal.getAttribute("name"));
    }

	public static void main(String[] args) {
		SpringApplication.run(OauthApplication.class, args);
	}

}
