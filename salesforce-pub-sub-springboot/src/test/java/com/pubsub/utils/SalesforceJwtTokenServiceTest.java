package com.pubsub.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubsub.config.SalesforceJwtConfig;
import com.pubsub.exceptions.SalesforceLoginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
public class SalesforceJwtTokenServiceTest {

    private SalesforceJwtTokenService tokenService;

    private MockRestServiceServer server;

    @MockBean
    private SalesforceJwtGenerator jwtGenerator;

    @MockBean
    private SalesforceJwtConfig jwtConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        tokenService = new SalesforceJwtTokenService(jwtConfig, jwtGenerator, builder);
        when(jwtConfig.getLoginUrl()).thenReturn("https://login.salesforce.com");
    }

    @Test
    void exchangeToken_Success_ReturnsResponse() throws Exception {
        when(jwtGenerator.generateToken()).thenReturn("mock-jwt");

        OAuthResponse mockResponse = new OAuthResponse();
        mockResponse.setAccessToken("test-token");
        mockResponse.setInstanceUrl("https://test.salesforce.com");

        server.expect(requestTo("https://login.salesforce.com/services/oauth2/token"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        OAuthResponse response = tokenService.exchangeToken();

        assertThat(response.getAccessToken()).isEqualTo("test-token");
        assertThat(response.getInstanceUrl()).isEqualTo("https://test.salesforce.com");
    }

    @Test
    void exchangeToken_GeneratorFails_ThrowsException() throws Exception {
        when(jwtGenerator.generateToken()).thenThrow(new RuntimeException("Gen error"));

        assertThatThrownBy(() -> tokenService.exchangeToken())
                .isInstanceOf(SalesforceLoginException.class)
                .hasMessageContaining("Failed to exchange JWT");
    }
}
