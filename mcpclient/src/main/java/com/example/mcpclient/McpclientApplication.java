package com.example.mcpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class McpclientApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpclientApplication.class, args);
	}

	@Bean
    WebClient.Builder webClientBuilder(McpSyncClientExchangeFilterFunction filterFunction) {
        return WebClient.builder()
                .filter(filterFunction);
    }

}
