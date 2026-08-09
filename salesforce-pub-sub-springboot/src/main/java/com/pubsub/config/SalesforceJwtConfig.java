package com.pubsub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "salesforce.jwt")
public class SalesforceJwtConfig {
    private String clientId;
    private String username;
    private String loginUrl;
    private String privateKeyPath;
}