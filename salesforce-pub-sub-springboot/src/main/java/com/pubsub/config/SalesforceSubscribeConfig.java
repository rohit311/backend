package com.pubsub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "salesforce-subscribe-config")
public class SalesforceSubscribeConfig {
    private String domainUrl;
    private String clientId;
    private String clientSecret;
    private Boolean eventListeningOn;
    private List<String> activeEvents;

    @PostConstruct
public void printConfig() {
    System.out.println("ACTIVE EVENTS = " + activeEvents);
}
}
