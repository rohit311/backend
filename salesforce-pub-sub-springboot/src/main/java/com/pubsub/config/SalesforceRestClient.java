package com.pubsub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Configuration
public class SalesforceRestClient {
    private final SalesforceSubscribeConfig salesforceSubscribeConfig;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(salesforceSubscribeConfig.getDomainUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
