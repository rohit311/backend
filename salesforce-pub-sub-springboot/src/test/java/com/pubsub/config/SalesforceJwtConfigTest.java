package com.pubsub.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {SalesforceJwtConfigTest.TestConfig.class})
@TestPropertySource(properties = {
        "salesforce.jwt.client-id=test-client-id",
        "salesforce.jwt.username=test-user@example.com",
        "salesforce.jwt.login-url=https://test.salesforce.com",
        "salesforce.jwt.private-key-path=classpath:test-key.jks"
})
public class SalesforceJwtConfigTest {

    @Autowired
    private SalesforceJwtConfig jwtConfig;

    @Test
    void propertiesAreBoundCorrectly() {
        assertThat(jwtConfig.getClientId()).isEqualTo("test-client-id");
        assertThat(jwtConfig.getUsername()).isEqualTo("test-user@example.com");
        assertThat(jwtConfig.getLoginUrl()).isEqualTo("https://test.salesforce.com");
        assertThat(jwtConfig.getPrivateKeyPath()).isEqualTo("classpath:test-key.jks");
    }

    @EnableConfigurationProperties(SalesforceJwtConfig.class)
    static class TestConfig {
    }
}
